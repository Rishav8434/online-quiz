package com.onlinequiz.dto.response;

import com.onlinequiz.enums.AttemptStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttemptResultResponse {

    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private String username;
    private Double score;
    private Double maxScore;
    private Double percentage;
    private Integer totalQuestions;
    private Integer attemptedQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer unansweredQuestions;
    private boolean passed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationTakenSeconds;
    private AttemptStatus status;
    private List<QuestionResultItem> questionResults = new ArrayList<>();

    public static class QuestionResultItem {
        private Long questionId;
        private String questionText;
        private Long selectedOptionId;
        private String selectedOptionText;
        private Long correctOptionId;
        private String correctOptionText;
        private boolean isCorrect;
        private Double marksAwarded;
        private List<OptionResponse> allOptions = new ArrayList<>();

        public QuestionResultItem() {}

        public QuestionResultItem(Long questionId, String questionText, Long selectedOptionId,
                                  String selectedOptionText, Long correctOptionId, String correctOptionText,
                                  boolean isCorrect, Double marksAwarded, List<OptionResponse> allOptions) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.selectedOptionId = selectedOptionId;
            this.selectedOptionText = selectedOptionText;
            this.correctOptionId = correctOptionId;
            this.correctOptionText = correctOptionText;
            this.isCorrect = isCorrect;
            this.marksAwarded = marksAwarded;
            this.allOptions = allOptions;
        }

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public Long getSelectedOptionId() { return selectedOptionId; }
        public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }

        public String getSelectedOptionText() { return selectedOptionText; }
        public void setSelectedOptionText(String selectedOptionText) { this.selectedOptionText = selectedOptionText; }

        public Long getCorrectOptionId() { return correctOptionId; }
        public void setCorrectOptionId(Long correctOptionId) { this.correctOptionId = correctOptionId; }

        public String getCorrectOptionText() { return correctOptionText; }
        public void setCorrectOptionText(String correctOptionText) { this.correctOptionText = correctOptionText; }

        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }

        public Double getMarksAwarded() { return marksAwarded; }
        public void setMarksAwarded(Double marksAwarded) { this.marksAwarded = marksAwarded; }

        public List<OptionResponse> getAllOptions() { return allOptions; }
        public void setAllOptions(List<OptionResponse> allOptions) { this.allOptions = allOptions; }
    }

    public AttemptResultResponse() {}

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getAttemptedQuestions() { return attemptedQuestions; }
    public void setAttemptedQuestions(Integer attemptedQuestions) { this.attemptedQuestions = attemptedQuestions; }

    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }

    public Integer getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(Integer wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public Integer getUnansweredQuestions() { return unansweredQuestions; }
    public void setUnansweredQuestions(Integer unansweredQuestions) { this.unansweredQuestions = unansweredQuestions; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDurationTakenSeconds() { return durationTakenSeconds; }
    public void setDurationTakenSeconds(Long durationTakenSeconds) { this.durationTakenSeconds = durationTakenSeconds; }

    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }

    public List<QuestionResultItem> getQuestionResults() { return questionResults; }
    public void setQuestionResults(List<QuestionResultItem> questionResults) { this.questionResults = questionResults; }
}
