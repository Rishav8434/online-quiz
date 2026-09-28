package com.onlinequiz.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_answers", indexes = {
    @Index(name = "idx_user_answers_attempt_id", columnList = "attempt_id"),
    @Index(name = "idx_user_answers_question_id", columnList = "question_id")
})
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt quizAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private Option selectedOption;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    @Column(name = "marks_awarded", nullable = false)
    private Double marksAwarded = 0.0;

    public UserAnswer() {}

    public UserAnswer(QuizAttempt quizAttempt, Question question, Option selectedOption, boolean isCorrect) {
        this.quizAttempt = quizAttempt;
        this.question = question;
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
        this.marksAwarded = 0.0;
    }

    public UserAnswer(QuizAttempt quizAttempt, Question question, Option selectedOption, boolean isCorrect, Double marksAwarded) {
        this.quizAttempt = quizAttempt;
        this.question = question;
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
        this.marksAwarded = marksAwarded != null ? marksAwarded : 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuizAttempt getQuizAttempt() {
        return quizAttempt;
    }

    public void setQuizAttempt(QuizAttempt quizAttempt) {
        this.quizAttempt = quizAttempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Option getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(Option selectedOption) {
        this.selectedOption = selectedOption;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public Double getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(Double marksAwarded) {
        this.marksAwarded = marksAwarded;
    }
}
