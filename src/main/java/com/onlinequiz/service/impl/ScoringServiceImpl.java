package com.onlinequiz.service.impl;

import com.onlinequiz.entity.Option;
import com.onlinequiz.entity.Question;
import com.onlinequiz.entity.Quiz;
import com.onlinequiz.service.ScoringService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ScoringServiceImpl implements ScoringService {

    @Override
    public ScoringResult evaluateQuiz(Quiz quiz, List<Question> questions, Map<Long, Long> answersMap) {
        double maxMarks = 0.0;
        double rawScore = 0.0;
        int attemptedCount = 0;
        int correctCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;

        List<EvaluatedAnswer> evaluatedAnswers = new ArrayList<>();

        for (Question question : questions) {
            double qMarks = question.getMarks() != null && question.getMarks() > 0 ? question.getMarks() : 1.0;
            maxMarks += qMarks;

            Long selectedOptionId = answersMap != null ? answersMap.get(question.getId()) : null;

            if (selectedOptionId == null) {
                unansweredCount++;
                evaluatedAnswers.add(new EvaluatedAnswer(question, null, false, 0.0));
                continue;
            }

            attemptedCount++;
            Option selectedOption = question.getOptions().stream()
                    .filter(opt -> opt.getId().equals(selectedOptionId))
                    .findFirst()
                    .orElse(null);

            if (selectedOption != null && selectedOption.isCorrect()) {
                correctCount++;
                rawScore += qMarks;
                evaluatedAnswers.add(new EvaluatedAnswer(question, selectedOption, true, qMarks));
            } else {
                wrongCount++;
                double deduction = 0.0;
                if (quiz.isNegativeMarking()) {
                    if (question.getNegativeMarks() != null && question.getNegativeMarks() > 0) {
                        deduction = question.getNegativeMarks();
                    } else if (quiz.getNegativeMarks() != null && quiz.getNegativeMarks() > 0) {
                        deduction = quiz.getNegativeMarks();
                    }
                }
                rawScore -= deduction;
                evaluatedAnswers.add(new EvaluatedAnswer(question, selectedOption, false, -deduction));
            }
        }

        // Apply non-negative floor policy for exam score
        double finalScore = Math.max(0.0, Math.round(rawScore * 100.0) / 100.0);
        double percentage = maxMarks > 0 ? (finalScore / maxMarks) * 100.0 : 0.0;
        percentage = Math.round(percentage * 100.0) / 100.0;

        double passThreshold = quiz.getPassPercentage() != null ? quiz.getPassPercentage() : 50.0;
        boolean passed = percentage >= passThreshold;

        return new ScoringResult(
                finalScore,
                maxMarks,
                percentage,
                questions.size(),
                attemptedCount,
                correctCount,
                wrongCount,
                unansweredCount,
                passed,
                evaluatedAnswers
        );
    }
}
