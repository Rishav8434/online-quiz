package com.onlinequiz.service;

import com.onlinequiz.entity.Option;
import com.onlinequiz.entity.Question;
import com.onlinequiz.entity.Quiz;

import java.util.List;
import java.util.Map;

public interface ScoringService {

    ScoringResult evaluateQuiz(Quiz quiz, List<Question> questions, Map<Long, Long> answersMap);

    class ScoringResult {
        private final double marksObtained;
        private final double maxMarks;
        private final double percentage;
        private final int totalQuestions;
        private final int attemptedCount;
        private final int correctCount;
        private final int wrongCount;
        private final int unansweredCount;
        private final boolean passed;
        private final List<EvaluatedAnswer> answers;

        public ScoringResult(double marksObtained, double maxMarks, double percentage,
                             int totalQuestions, int attemptedCount, int correctCount,
                             int wrongCount, int unansweredCount, boolean passed,
                             List<EvaluatedAnswer> answers) {
            this.marksObtained = marksObtained;
            this.maxMarks = maxMarks;
            this.percentage = percentage;
            this.totalQuestions = totalQuestions;
            this.attemptedCount = attemptedCount;
            this.correctCount = correctCount;
            this.wrongCount = wrongCount;
            this.unansweredCount = unansweredCount;
            this.passed = passed;
            this.answers = answers;
        }

        public double getMarksObtained() { return marksObtained; }
        public double getMaxMarks() { return maxMarks; }
        public double getPercentage() { return percentage; }
        public int getTotalQuestions() { return totalQuestions; }
        public int getAttemptedCount() { return attemptedCount; }
        public int getCorrectCount() { return correctCount; }
        public int getWrongCount() { return wrongCount; }
        public int getUnansweredCount() { return unansweredCount; }
        public boolean isPassed() { return passed; }
        public List<EvaluatedAnswer> getAnswers() { return answers; }
    }

    class EvaluatedAnswer {
        private final Question question;
        private final Option selectedOption;
        private final boolean isCorrect;
        private final double marksAwarded;

        public EvaluatedAnswer(Question question, Option selectedOption, boolean isCorrect, double marksAwarded) {
            this.question = question;
            this.selectedOption = selectedOption;
            this.isCorrect = isCorrect;
            this.marksAwarded = marksAwarded;
        }

        public Question getQuestion() { return question; }
        public Option getSelectedOption() { return selectedOption; }
        public boolean isCorrect() { return isCorrect; }
        public double getMarksAwarded() { return marksAwarded; }
    }
}
