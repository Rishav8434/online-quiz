package com.onlinequiz.mapper;

import com.onlinequiz.dto.response.AttemptResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.dto.response.LeaderboardEntryResponse;
import com.onlinequiz.dto.response.OptionResponse;
import com.onlinequiz.dto.response.QuestionResponse;
import com.onlinequiz.entity.Option;
import com.onlinequiz.entity.Question;
import com.onlinequiz.entity.QuizAttempt;
import com.onlinequiz.entity.UserAnswer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AttemptMapper {

    private final QuizMapper quizMapper;

    public AttemptMapper(QuizMapper quizMapper) {
        this.quizMapper = quizMapper;
    }

    public AttemptResponse toAttemptResponse(QuizAttempt attempt, long remainingSeconds) {
        List<QuestionResponse> questions = attempt.getQuiz().getQuestions().stream()
                .filter(q -> !q.isDeleted())
                .map(q -> quizMapper.toQuestionResponse(q, false))
                .collect(Collectors.toList());

        return new AttemptResponse(
                attempt.getId(),
                attempt.getQuiz().getId(),
                attempt.getQuiz().getTitle(),
                attempt.getQuiz().getDurationMinutes(),
                attempt.getStartTime(),
                Math.max(0, remainingSeconds),
                attempt.getStatus(),
                questions
        );
    }

    public AttemptResultResponse toResultResponse(QuizAttempt attempt) {
        long durationTaken = 0;
        if (attempt.getEndTime() != null) {
            durationTaken = Duration.between(attempt.getStartTime(), attempt.getEndTime()).getSeconds();
        } else {
            durationTaken = Duration.between(attempt.getStartTime(), LocalDateTime.now()).getSeconds();
        }

        Map<Long, UserAnswer> answerMap = attempt.getUserAnswers().stream()
                .collect(Collectors.toMap(
                        ua -> ua.getQuestion().getId(),
                        ua -> ua,
                        (existing, replacement) -> existing
                ));

        List<AttemptResultResponse.QuestionResultItem> questionResults = new ArrayList<>();
        if (attempt.getQuiz().getQuestions() != null) {
            for (Question question : attempt.getQuiz().getQuestions()) {
                if (question.isDeleted()) continue;

                UserAnswer ua = answerMap.get(question.getId());

                Long selectedOptionId = (ua != null && ua.getSelectedOption() != null) ? ua.getSelectedOption().getId() : null;
                String selectedOptionText = (ua != null && ua.getSelectedOption() != null) ? ua.getSelectedOption().getText() : "Not answered";
                boolean isCorrect = ua != null && ua.isCorrect();
                Double marksAwarded = ua != null ? ua.getMarksAwarded() : 0.0;

                Option correctOption = question.getOptions().stream()
                        .filter(Option::isCorrect)
                        .findFirst()
                        .orElse(null);

                Long correctOptionId = correctOption != null ? correctOption.getId() : null;
                String correctOptionText = correctOption != null ? correctOption.getText() : "N/A";

                List<OptionResponse> optionResponses = question.getOptions().stream()
                        .map(o -> new OptionResponse(o.getId(), o.getText(), o.isCorrect()))
                        .collect(Collectors.toList());

                questionResults.add(new AttemptResultResponse.QuestionResultItem(
                        question.getId(),
                        question.getText(),
                        selectedOptionId,
                        selectedOptionText,
                        correctOptionId,
                        correctOptionText,
                        isCorrect,
                        marksAwarded,
                        optionResponses
                ));
            }
        }

        AttemptResultResponse response = new AttemptResultResponse();
        response.setAttemptId(attempt.getId());
        response.setQuizId(attempt.getQuiz().getId());
        response.setQuizTitle(attempt.getQuiz().getTitle());
        response.setUsername(attempt.getUser().getUsername());
        response.setScore(attempt.getScore());
        response.setMaxScore(attempt.getMaxScore());
        response.setPercentage(attempt.getPercentage());
        response.setTotalQuestions(attempt.getTotalQuestions());
        response.setAttemptedQuestions(attempt.getAttemptedQuestions());
        response.setCorrectAnswers(attempt.getCorrectAnswers());
        response.setWrongAnswers(attempt.getWrongAnswers());
        response.setUnansweredQuestions(attempt.getUnansweredQuestions());
        response.setPassed(attempt.isPassed());
        response.setStartTime(attempt.getStartTime());
        response.setEndTime(attempt.getEndTime());
        response.setDurationTakenSeconds(durationTaken);
        response.setStatus(attempt.getStatus());
        response.setQuestionResults(questionResults);

        return response;
    }

    public LeaderboardEntryResponse toLeaderboardEntry(QuizAttempt attempt, int rank) {
        long duration = 0;
        if (attempt.getEndTime() != null) {
            duration = Duration.between(attempt.getStartTime(), attempt.getEndTime()).getSeconds();
        }

        return new LeaderboardEntryResponse(
                rank,
                attempt.getId(),
                attempt.getUser().getUsername(),
                attempt.getUser().getFullName(),
                attempt.getScore(),
                attempt.getPercentage(),
                duration,
                attempt.getStartTime()
        );
    }
}
