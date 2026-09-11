package com.college.feedback.repository;

import com.college.feedback.entity.FeedbackForm;
import com.college.feedback.entity.enums.FormStatus;
import com.college.feedback.entity.enums.TargetAudience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackFormRepository extends JpaRepository<FeedbackForm, UUID> {
    List<FeedbackForm> findByIsActiveTrue();
    List<FeedbackForm> findByStatus(FormStatus status);
    Long countByStatus(FormStatus status);
    List<FeedbackForm> findByTargetAudienceInAndIsActiveTrue(List<TargetAudience> audiences);

    @Query("SELECT f FROM FeedbackForm f WHERE f.isActive = true AND (f.targetDepartment IS NULL OR f.targetDepartment = :dept) AND f.targetAudience IN :audiences")
    List<FeedbackForm> findActiveFormsForDepartmentAndAudience(@Param("dept") String dept, @Param("audiences") List<TargetAudience> audiences);
}
