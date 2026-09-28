package com.onlinequiz.repository;

import com.onlinequiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdAndIsDeletedFalse(Long quizId);

    Optional<Question> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id = :id AND q.isDeleted = false")
    Optional<Question> findByIdWithOptions(@Param("id") Long id);

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId AND q.isDeleted = false")
    List<Question> findByQuizIdWithOptions(@Param("quizId") Long quizId);

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId AND q.isDeleted = false")
    List<Question> findByQuizIdWithOptionsAndIsDeletedFalse(@Param("quizId") Long quizId);
}
