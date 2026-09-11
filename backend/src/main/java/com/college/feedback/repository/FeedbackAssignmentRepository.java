package com.college.feedback.repository;

import com.college.feedback.entity.FeedbackAssignment;
import com.college.feedback.entity.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackAssignmentRepository extends JpaRepository<FeedbackAssignment, UUID> {
    List<FeedbackAssignment> findByUserId(UUID userId);
    List<FeedbackAssignment> findByUserIdAndStatus(UUID userId, AssignmentStatus status);
    Optional<FeedbackAssignment> findByUserIdAndFormId(UUID userId, UUID formId);
    boolean existsByFormIdAndUserId(UUID formId, UUID userId);
    Long countByUserIdAndStatus(UUID userId, AssignmentStatus status);
    List<FeedbackAssignment> findByFormId(UUID formId);
    Long countByFormId(UUID formId);
    Long countByFormIdAndStatus(UUID formId, AssignmentStatus status);
    Long countByStatus(AssignmentStatus status);

    @Query("SELECT fa FROM FeedbackAssignment fa JOIN FETCH fa.form f WHERE fa.user.id = :userId ORDER BY f.endDate ASC")
    List<FeedbackAssignment> findAllWithFormByUserId(@Param("userId") UUID userId);
}
