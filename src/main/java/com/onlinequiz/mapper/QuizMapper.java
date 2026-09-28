package com.onlinequiz.mapper;

import com.onlinequiz.dto.request.CreateOptionRequest;
import com.onlinequiz.dto.request.CreateQuestionRequest;
import com.onlinequiz.dto.request.CreateQuizRequest;
import com.onlinequiz.dto.response.OptionResponse;
import com.onlinequiz.dto.response.QuestionResponse;
import com.onlinequiz.dto.response.QuizResponse;
import com.onlinequiz.dto.response.QuizSummaryResponse;
import com.onlinequiz.entity.Option;
import com.onlinequiz.entity.Question;
import com.onlinequiz.entity.Quiz;
import com.onlinequiz.entity.User;
import com.onlinequiz.enums.QuizStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuizMapper {

    public Quiz toEntity(CreateQuizRequest request, User creator) {
        Quiz quiz = new Quiz(
                request.getTitle(),
                request.getDescription(),
                request.getDurationMinutes(),
                creator
        );

        if (request.getNegativeMarking() != null) {
            quiz.setNegativeMarking(request.getNegativeMarking());
        }
        if (request.getNegativeMarks() != null) {
            quiz.setNegativeMarks(request.getNegativeMarks());
        }
        if (request.getPassPercentage() != null) {
            quiz.setPassPercentage(request.getPassPercentage());
        }

        if (request.getQuestions() != null) {
            for (CreateQuestionRequest qReq : request.getQuestions()) {
                Question question = new Question(
                        qReq.getText(),
                        qReq.getMarks() != null ? qReq.getMarks() : 1.0,
                        qReq.getNegativeMarks() != null ? qReq.getNegativeMarks() : (quiz.isNegativeMarking() ? quiz.getNegativeMarks() : 0.0),
                        quiz
                );
                if (qReq.getOptions() != null) {
                    for (CreateOptionRequest oReq : qReq.getOptions()) {
                        Option option = new Option(oReq.getText(), Boolean.TRUE.equals(oReq.getIsCorrect()), question);
                        question.addOption(option);
                    }
                }
                quiz.addQuestion(question);
            }
        }

        quiz.recalculateTotalMarks();
        return quiz;
    }

    public QuizSummaryResponse toSummaryResponse(Quiz quiz) {
        if (quiz == null) return null;
        int totalQuestions = quiz.getQuestions() != null ? (int) quiz.getQuestions().stream().filter(q -> !q.isDeleted()).count() : 0;
        String creatorName = quiz.getCreatedBy() != null ? quiz.getCreatedBy().getFullName() : "Admin";

        return new QuizSummaryResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getDurationMinutes(),
                totalQuestions,
                quiz.getTotalMarks(),
                quiz.getStatus() != null ? quiz.getStatus() : QuizStatus.DRAFT,
                quiz.isNegativeMarking(),
                quiz.getPassPercentage(),
                creatorName,
                quiz.getCreatedAt()
        );
    }

    public QuizResponse toResponse(Quiz quiz, boolean includeAnswers) {
        if (quiz == null) return null;

        List<QuestionResponse> questionResponses = quiz.getQuestions() != null
                ? quiz.getQuestions().stream()
                .filter(q -> !q.isDeleted())
                .map(q -> toQuestionResponse(q, includeAnswers))
                .collect(Collectors.toList())
                : List.of();

        String creatorName = quiz.getCreatedBy() != null ? quiz.getCreatedBy().getFullName() : "Admin";

        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getDurationMinutes(),
                quiz.getStatus() != null ? quiz.getStatus() : QuizStatus.DRAFT,
                quiz.getTotalMarks(),
                quiz.isNegativeMarking(),
                quiz.getNegativeMarks(),
                quiz.getPassPercentage(),
                creatorName,
                quiz.getCreatedAt(),
                questionResponses.size(),
                questionResponses
        );
    }

    public QuestionResponse toQuestionResponse(Question question, boolean includeAnswers) {
        if (question == null) return null;

        List<OptionResponse> optionResponses = question.getOptions() != null
                ? question.getOptions().stream()
                .map(o -> new OptionResponse(
                        o.getId(),
                        o.getText(),
                        includeAnswers ? o.isCorrect() : null
                ))
                .collect(Collectors.toList())
                : List.of();

        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getMarks() != null ? question.getMarks() : 1.0,
                question.getNegativeMarks() != null ? question.getNegativeMarks() : 0.0,
                optionResponses
        );
    }
}
