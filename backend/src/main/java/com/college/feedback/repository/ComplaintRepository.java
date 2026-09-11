package com.college.feedback.repository;

import com.college.feedback.entity.Complaint;
import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
    List<Complaint> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
    List<Complaint> findAllByOrderByCreatedAtDesc();
    List<Complaint> findByStatus(IssueStatus status);
    List<Complaint> findByPriority(Priority priority);
    Optional<Complaint> findByTicketNumber(String ticketNumber);
    Boolean existsByTicketNumber(String ticketNumber);
    
    Long countByStatus(IssueStatus status);
    Long countByStatusNot(IssueStatus status);
    Long countByStatusNotIn(Collection<IssueStatus> statuses);
    Long countByPriority(Priority priority);
    Long countByPriorityInAndStatusNotIn(Collection<Priority> priorities, Collection<IssueStatus> statuses);
    Long countByStudentIdAndStatusNot(UUID studentId, IssueStatus status);
    Long countByStudentIdAndStatusNotIn(UUID studentId, Collection<IssueStatus> statuses);

    List<Complaint> findByPublicVisibleTrueAndStatusNotOrderByCreatedAtDesc(IssueStatus status);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:category IS NULL OR LOWER(c.category) = LOWER(:category)) AND " +
           "(:assignedCell IS NULL OR LOWER(c.assignedCell) LIKE LOWER(CONCAT('%', :assignedCell, '%'))) " +
           "ORDER BY c.createdAt DESC")
    List<Complaint> findWithFilters(@Param("status") IssueStatus status,
                                    @Param("priority") Priority priority,
                                    @Param("category") String category,
                                    @Param("assignedCell") String assignedCell);
}
