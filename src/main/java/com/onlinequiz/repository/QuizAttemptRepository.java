package com.onlinequiz.repository;

import com.onlinequiz.entity.QuizAttempt;
import com.onlinequiz.enums.AttemptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserIdOrderByStartTimeDesc(Long userId);

    Page<QuizAttempt> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);

    Page<QuizAttempt> findAllByOrderByStartTimeDesc(Pageable pageable);

    List<QuizAttempt> findByQuizIdOrderByScoreDesc(Long quizId);

    Optional<QuizAttempt> findFirstByUserIdAndQuizIdAndStatusOrderByStartTimeDesc(Long userId, Long quizId, AttemptStatus status);

    @Query("SELECT qa FROM QuizAttempt qa LEFT JOIN FETCH qa.userAnswers ua LEFT JOIN FETCH ua.question LEFT JOIN FETCH ua.selectedOption WHERE qa.id = :id")
    Optional<QuizAttempt> findByIdWithAnswers(@Param("id") Long id);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.status IN (com.onlinequiz.enums.AttemptStatus.SUBMITTED, com.onlinequiz.enums.AttemptStatus.AUTO_SUBMITTED) ORDER BY qa.score DESC, qa.percentage DESC")
    List<QuizAttempt> findLeaderboardByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId")
    long countAttemptsByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT AVG(qa.percentage) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.status IN (com.onlinequiz.enums.AttemptStatus.SUBMITTED, com.onlinequiz.enums.AttemptStatus.AUTO_SUBMITTED)")
    Double findAveragePercentageByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.status IN (com.onlinequiz.enums.AttemptStatus.SUBMITTED, com.onlinequiz.enums.AttemptStatus.AUTO_SUBMITTED)")
    Double findAverageScoreByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT MAX(qa.score) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.status IN (com.onlinequiz.enums.AttemptStatus.SUBMITTED, com.onlinequiz.enums.AttemptStatus.AUTO_SUBMITTED)")
    Double findHighestScoreByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT MIN(qa.score) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.status IN (com.onlinequiz.enums.AttemptStatus.SUBMITTED, com.onlinequiz.enums.AttemptStatus.AUTO_SUBMITTED)")
    Double findLowestScoreByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT AVG(qa.percentage) FROM QuizAttempt qa WHERE qa.status IN (com.onlinequiz.enums.AttemptStatus.SUBMITTED, com.onlinequiz.enums.AttemptStatus.AUTO_SUBMITTED)")
    Double findOverallAveragePercentage();

    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.status IN (com.onlinequiz.enums.AttemptStatus.SUBMITTED, com.onlinequiz.enums.AttemptStatus.AUTO_SUBMITTED)")
    Double findOverallAverageScore();

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.status IN (com.onlinequiz.enums.AttemptStatus.SUBMITTED, com.onlinequiz.enums.AttemptStatus.AUTO_SUBMITTED) AND qa.passed = true")
    long countPassedAttempts();

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.status IN (com.onlinequiz.enums.AttemptStatus.SUBMITTED, com.onlinequiz.enums.AttemptStatus.AUTO_SUBMITTED)")
    long countSubmittedAttempts();

    List<QuizAttempt> findByQuizIdAndStatusIn(Long quizId, Collection<AttemptStatus> statuses);
}
