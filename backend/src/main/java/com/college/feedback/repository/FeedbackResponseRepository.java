package com.college.feedback.repository;

import com.college.feedback.entity.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, UUID> {
    List<FeedbackResponse> findByFormId(UUID formId);
    List<FeedbackResponse> findByUserId(UUID userId);
    Long countByFormId(UUID formId);
    boolean existsByFormIdAndUserId(UUID formId, UUID userId);

    @Query("SELECT AVG(fr.overallRating) FROM FeedbackResponse fr WHERE fr.form.id = :formId")
    Double getAverageRatingForForm(@Param("formId") UUID formId);

    @Query("SELECT AVG(fr.overallRating) FROM FeedbackResponse fr")
    Double getGlobalAverageRating();

    @Query("SELECT fr.overallRating, COUNT(fr) FROM FeedbackResponse fr WHERE fr.overallRating IS NOT NULL GROUP BY fr.overallRating")
    List<Object[]> getRatingDistribution();

    @Query("SELECT fr.overallRating, COUNT(fr) FROM FeedbackResponse fr WHERE fr.form.id = :formId AND fr.overallRating IS NOT NULL GROUP BY fr.overallRating")
    List<Object[]> getRatingDistributionForForm(@Param("formId") UUID formId);

    @Query("SELECT fr.form.category, AVG(fr.overallRating), COUNT(fr) FROM FeedbackResponse fr WHERE fr.form.category IS NOT NULL GROUP BY fr.form.category")
    List<Object[]> getCategoryAnalytics();
}
