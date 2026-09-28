package com.onlinequiz.repository;

import com.onlinequiz.entity.Quiz;
import com.onlinequiz.enums.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByIdAndIsDeletedFalse(Long id);

    List<Quiz> findByStatusAndIsDeletedFalse(QuizStatus status);

    Page<Quiz> findByStatusAndIsDeletedFalse(QuizStatus status, Pageable pageable);

    Page<Quiz> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT q FROM Quiz q WHERE q.isDeleted = false AND (:status IS NULL OR q.status = :status) " +
           "AND (:search IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(q.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Quiz> searchQuizzes(@Param("status") QuizStatus status,
                             @Param("search") String search,
                             Pageable pageable);

    List<Quiz> findByCreatedByIdAndIsDeletedFalse(Long userId);

    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions quest " +
           "WHERE q.id = :id AND q.isDeleted = false AND (quest IS NULL OR quest.isDeleted = false)")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    long countByIsDeletedFalse();

    long countByStatusAndIsDeletedFalse(QuizStatus status);
}
