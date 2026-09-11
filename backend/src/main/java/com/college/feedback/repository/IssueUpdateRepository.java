package com.college.feedback.repository;

import com.college.feedback.entity.IssueUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueUpdateRepository extends JpaRepository<IssueUpdate, UUID> {
    List<IssueUpdate> findByComplaintIdOrderByCreatedAtAsc(UUID complaintId);
    List<IssueUpdate> findByRequestIdOrderByCreatedAtAsc(UUID requestId);
}
