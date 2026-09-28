package com.onlinequiz.service.impl;

import com.onlinequiz.dto.request.CreateOptionRequest;
import com.onlinequiz.dto.request.CreateQuestionRequest;
import com.onlinequiz.dto.request.CreateQuizRequest;
import com.onlinequiz.dto.request.UpdateQuizRequest;
import com.onlinequiz.dto.response.QuizResponse;
import com.onlinequiz.dto.response.QuizSummaryResponse;
import com.onlinequiz.entity.Quiz;
import com.onlinequiz.entity.User;
import com.onlinequiz.enums.QuizStatus;
import com.onlinequiz.exception.BadRequestException;
import com.onlinequiz.exception.ResourceNotFoundException;
import com.onlinequiz.mapper.QuizMapper;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizMapper quizMapper;

    public QuizServiceImpl(QuizRepository quizRepository,
                           UserRepository userRepository,
                           QuizMapper quizMapper) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.quizMapper = quizMapper;
    }

    @Override
    @Transactional
    public QuizResponse createQuiz(CreateQuizRequest request, Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new BadRequestException("A quiz must have at least one question");
        }

        for (int i = 0; i < request.getQuestions().size(); i++) {
            CreateQuestionRequest q = request.getQuestions().get(i);
            if (q.getOptions() == null || q.getOptions().size() < 2) {
                throw new BadRequestException("Question " + (i + 1) + " must have at least 2 options");
            }
            long correctCount = q.getOptions().stream()
                    .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                    .count();
            if (correctCount == 0) {
                throw new BadRequestException("Question " + (i + 1) + " must have at least one correct option");
            }
            if (correctCount > 1) {
                throw new BadRequestException("Question " + (i + 1) + " must have exactly one correct option");
            }
        }

        Quiz quiz = quizMapper.toEntity(request, creator);
        Quiz savedQuiz = quizRepository.save(quiz);

        log.info("Created quiz id={} by userId={}", savedQuiz.getId(), userId);
        return quizMapper.toResponse(savedQuiz, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryResponse> getAllPublishedQuizzes() {
        return quizRepository.findByStatusAndIsDeletedFalse(QuizStatus.PUBLISHED).stream()
                .map(quizMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizSummaryResponse> getPublishedQuizzes(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return quizRepository.searchQuizzes(QuizStatus.PUBLISHED, search.trim(), pageable)
                    .map(quizMapper::toSummaryResponse);
        }
        return quizRepository.findByStatusAndIsDeletedFalse(QuizStatus.PUBLISHED, pageable)
                .map(quizMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryResponse> getAllQuizzesForAdmin() {
        return quizRepository.findAll().stream()
                .filter(q -> !q.isDeleted())
                .map(quizMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizSummaryResponse> getAllQuizzesForAdmin(QuizStatus status, String search, Pageable pageable) {
        if ((search != null && !search.trim().isEmpty()) || status != null) {
            String q = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
            return quizRepository.searchQuizzes(status, q, pageable)
                    .map(quizMapper::toSummaryResponse);
        }
        return quizRepository.findByIsDeletedFalse(pageable)
                .map(quizMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long id, boolean isAdmin) {
        Quiz quiz = quizRepository.findByIdWithQuestions(id)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", id));

        if (!isAdmin && quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Quiz", "id", id);
        }

        return quizMapper.toResponse(quiz, isAdmin);
    }

    @Override
    @Transactional
    public QuizResponse updateQuiz(Long id, UpdateQuizRequest request, Long userId) {
        Quiz quiz = quizRepository.findByIdWithQuestions(id)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", id));

        quiz.setTitle(request.getTitle());
        if (request.getDescription() != null) {
            quiz.setDescription(request.getDescription());
        }
        quiz.setDurationMinutes(request.getDurationMinutes());
        if (request.getStatus() != null) {
            quiz.setStatus(request.getStatus());
        }
        if (request.getNegativeMarking() != null) {
            quiz.setNegativeMarking(request.getNegativeMarking());
        }
        if (request.getNegativeMarks() != null) {
            quiz.setNegativeMarks(request.getNegativeMarks());
        }
        if (request.getPassPercentage() != null) {
            quiz.setPassPercentage(request.getPassPercentage());
        }

        quiz.recalculateTotalMarks();
        Quiz updatedQuiz = quizRepository.save(quiz);

        log.info("Updated quiz id={} by userId={}", id, userId);
        return quizMapper.toResponse(updatedQuiz, true);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long id, Long userId) {
        Quiz quiz = quizRepository.findById(id)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", id));

        quiz.setDeleted(true);
        quizRepository.save(quiz);
        log.info("Soft-deleted quiz id={} by userId={}", id, userId);
    }

    @Override
    @Transactional
    public QuizResponse publishQuiz(Long id, Long userId) {
        Quiz quiz = quizRepository.findByIdWithQuestions(id)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", id));

        long activeQuestionsCount = quiz.getQuestions() != null
                ? quiz.getQuestions().stream().filter(q -> !q.isDeleted()).count()
                : 0;

        if (activeQuestionsCount == 0) {
            throw new BadRequestException("Cannot publish a quiz with no active questions");
        }

        quiz.setStatus(QuizStatus.PUBLISHED);
        Quiz saved = quizRepository.save(quiz);
        log.info("Published quiz id={} by userId={}", id, userId);
        return quizMapper.toResponse(saved, true);
    }

    @Override
    @Transactional
    public QuizResponse unpublishQuiz(Long id, Long userId) {
        Quiz quiz = quizRepository.findById(id)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", id));

        quiz.setStatus(QuizStatus.DRAFT);
        Quiz saved = quizRepository.save(quiz);
        log.info("Unpublished quiz id={} by userId={}", id, userId);
        return quizMapper.toResponse(saved, true);
    }
}
