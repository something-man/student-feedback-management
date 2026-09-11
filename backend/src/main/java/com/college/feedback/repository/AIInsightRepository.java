package com.college.feedback.repository;

import com.college.feedback.entity.AIInsight;
import com.college.feedback.entity.enums.InsightType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AIInsightRepository extends JpaRepository<AIInsight, UUID> {
    List<AIInsight> findAllByOrderByCreatedAtDesc();
    List<AIInsight> findByInsightType(InsightType insightType);
}
