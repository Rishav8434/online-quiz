package com.onlinequiz.service.impl;

import com.onlinequiz.dto.request.SubmitAnswerRequest;
import com.onlinequiz.dto.request.SubmitQuizRequest;
import com.onlinequiz.dto.response.AttemptResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.entity.Option;
import com.onlinequiz.entity.Question;
import com.onlinequiz.entity.Quiz;
import com.onlinequiz.entity.QuizAttempt;
import com.onlinequiz.entity.User;
import com.onlinequiz.entity.UserAnswer;
import com.onlinequiz.enums.AttemptStatus;
import com.onlinequiz.enums.QuizStatus;
import com.onlinequiz.exception.BadRequestException;
import com.onlinequiz.exception.QuizAlreadySubmittedException;
import com.onlinequiz.exception.ResourceNotFoundException;
import com.onlinequiz.exception.UnauthorizedException;
import com.onlinequiz.mapper.AttemptMapper;
import com.onlinequiz.repository.OptionRepository;
import com.onlinequiz.repository.QuestionRepository;
import com.onlinequiz.repository.QuizAttemptRepository;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.service.AttemptService;
import com.onlinequiz.service.EmailService;
import com.onlinequiz.service.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttemptServiceImpl implements AttemptService {

    private static final Logger log = LoggerFactory.getLogger(AttemptServiceImpl.class);

    private final QuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ScoringService scoringService;
    private final AttemptMapper attemptMapper;
    private final EmailService emailService;

    public AttemptServiceImpl(QuizAttemptRepository attemptRepository,
                              QuizRepository quizRepository,
                              UserRepository userRepository,
                              QuestionRepository questionRepository,
                              OptionRepository optionRepository,
                              ScoringService scoringService,
                              AttemptMapper attemptMapper,
                              EmailService emailService) {
        this.attemptRepository = attemptRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.scoringService = scoringService;
        this.attemptMapper = attemptMapper;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public AttemptResponse startAttempt(Long quizId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));

        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new BadRequestException("This quiz is currently not published for attempts");
        }

        long activeQuestionsCount = quiz.getQuestions() != null
                ? quiz.getQuestions().stream().filter(q -> !q.isDeleted()).count()
                : 0;

        if (activeQuestionsCount == 0) {
            throw new BadRequestException("Quiz has no questions available");
        }

        // Check for an active IN_PROGRESS attempt
        Optional<QuizAttempt> existingAttemptOpt = attemptRepository
                .findFirstByUserIdAndQuizIdAndStatusOrderByStartTimeDesc(userId, quizId, AttemptStatus.IN_PROGRESS);

        if (existingAttemptOpt.isPresent()) {
            QuizAttempt existingAttempt = existingAttemptOpt.get();
            long maxAllowedSeconds = (long) quiz.getDurationMinutes() * 60;
            long elapsedSeconds = Duration.between(existingAttempt.getStartTime(), LocalDateTime.now()).getSeconds();

            if (elapsedSeconds <= maxAllowedSeconds) {
                long remainingSeconds = maxAllowedSeconds - elapsedSeconds;
                return attemptMapper.toAttemptResponse(existingAttempt, remainingSeconds);
            } else {
                finalizeExpiredAttempt(existingAttempt);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        QuizAttempt newAttempt = new QuizAttempt(quiz, user, now);
        newAttempt.setExpectedEndTime(now.plusMinutes(quiz.getDurationMinutes()));
        newAttempt.setTotalQuestions((int) activeQuestionsCount);
        newAttempt.setMaxScore(quiz.getTotalMarks());

        QuizAttempt savedAttempt = attemptRepository.save(newAttempt);
        log.info("Started quiz attempt id={} for quizId={} by userId={}", savedAttempt.getId(), quizId, userId);

        long remainingSeconds = (long) quiz.getDurationMinutes() * 60;
        return attemptMapper.toAttemptResponse(savedAttempt, remainingSeconds);
    }

    @Override
    @Transactional
    public AttemptResultResponse submitAttempt(Long attemptId, SubmitQuizRequest request, Long userId) {
        QuizAttempt attempt = attemptRepository.findByIdWithAnswers(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "id", attemptId));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to submit this attempt");
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new QuizAlreadySubmittedException("This quiz attempt has already been submitted or finalized");
        }

        Quiz quiz = attempt.getQuiz();
        LocalDateTime now = LocalDateTime.now();
        long maxAllowedSeconds = (long) quiz.getDurationMinutes() * 60;
        long elapsedSeconds = Duration.between(attempt.getStartTime(), now).getSeconds();

        // 15 seconds grace period for network latency
        boolean isExpired = elapsedSeconds > (maxAllowedSeconds + 15);
        attempt.setStatus(isExpired ? AttemptStatus.AUTO_SUBMITTED : AttemptStatus.SUBMITTED);
        attempt.setEndTime(now);

        // Map submitted answers
        Map<Long, Long> answersMap = new HashMap<>();
        if (request != null && request.getAnswers() != null) {
            for (SubmitAnswerRequest ans : request.getAnswers()) {
                if (ans.getQuestionId() != null && ans.getSelectedOptionId() != null) {
                    answersMap.put(ans.getQuestionId(), ans.getSelectedOptionId());
                }
            }
        }

        List<Question> questions = questionRepository.findByQuizIdWithOptionsAndIsDeletedFalse(quiz.getId());
        ScoringService.ScoringResult scoringResult = scoringService.evaluateQuiz(quiz, questions, answersMap);

        attempt.setTotalQuestions(scoringResult.getTotalQuestions());
        attempt.setAttemptedQuestions(scoringResult.getAttemptedCount());
        attempt.setCorrectAnswers(scoringResult.getCorrectCount());
        attempt.setWrongAnswers(scoringResult.getWrongCount());
        attempt.setUnansweredQuestions(scoringResult.getUnansweredCount());
        attempt.setScore(scoringResult.getMarksObtained());
        attempt.setMaxScore(scoringResult.getMaxMarks());
        attempt.setPercentage(scoringResult.getPercentage());
        attempt.setPassed(scoringResult.isPassed());

        attempt.getUserAnswers().clear();
        for (ScoringService.EvaluatedAnswer ea : scoringResult.getAnswers()) {
            UserAnswer ua = new UserAnswer(attempt, ea.getQuestion(), ea.getSelectedOption(), ea.isCorrect());
            ua.setMarksAwarded(ea.getMarksAwarded());
            attempt.addUserAnswer(ua);
        }

        QuizAttempt savedAttempt = attemptRepository.save(attempt);
        log.info("Submitted quiz attempt id={}, status={}, score={}/{}, percentage={}%",
                savedAttempt.getId(), savedAttempt.getStatus(), savedAttempt.getScore(),
                savedAttempt.getMaxScore(), savedAttempt.getPercentage());

        // Send Result Email asynchronously
        try {
            String resultUrl = "http://localhost:8080/results/" + savedAttempt.getId();
            emailService.sendQuizResultEmail(
                    savedAttempt.getUser().getEmail(),
                    savedAttempt.getUser().getFullName(),
                    quiz.getTitle(),
                    savedAttempt.getScore(),
                    savedAttempt.getPercentage(),
                    resultUrl
            );
        } catch (Exception e) {
            log.warn("Failed to dispatch quiz result email: {}", e.getMessage());
        }

        return attemptMapper.toResultResponse(savedAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public AttemptResultResponse getAttemptResult(Long attemptId, Long userId) {
        QuizAttempt attempt = attemptRepository.findByIdWithAnswers(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "id", attemptId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getName().name()));

        if (!attempt.getUser().getId().equals(userId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to view this result");
        }

        return attemptMapper.toResultResponse(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttemptResultResponse> getUserAttemptHistory(Long userId) {
        List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByStartTimeDesc(userId);
        return attempts.stream()
                .map(attemptMapper::toResultResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttemptResultResponse> getUserAttemptHistory(Long userId, Pageable pageable) {
        return attemptRepository.findByUserIdOrderByStartTimeDesc(userId, pageable)
                .map(attemptMapper::toResultResponse);
    }

    private void finalizeExpiredAttempt(QuizAttempt attempt) {
        attempt.setStatus(AttemptStatus.AUTO_SUBMITTED);
        LocalDateTime endTime = attempt.getStartTime().plusMinutes(attempt.getQuiz().getDurationMinutes());
        attempt.setEndTime(endTime);

        List<Question> questions = questionRepository.findByQuizIdWithOptionsAndIsDeletedFalse(attempt.getQuiz().getId());
        ScoringService.ScoringResult scoringResult = scoringService.evaluateQuiz(attempt.getQuiz(), questions, Map.of());

        attempt.setTotalQuestions(scoringResult.getTotalQuestions());
        attempt.setAttemptedQuestions(0);
        attempt.setCorrectAnswers(0);
        attempt.setWrongAnswers(0);
        attempt.setUnansweredQuestions(scoringResult.getTotalQuestions());
        attempt.setScore(0.0);
        attempt.setMaxScore(scoringResult.getMaxMarks());
        attempt.setPercentage(0.0);
        attempt.setPassed(false);

        attemptRepository.save(attempt);
        log.info("Auto-submitted expired attempt id={}", attempt.getId());
    }
}
