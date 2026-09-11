package com.college.feedback.repository;

import com.college.feedback.entity.Request;
import com.college.feedback.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {
    List<Request> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
    List<Request> findAllByOrderByCreatedAtDesc();
    List<Request> findByStatus(RequestStatus status);
    Optional<Request> findByRequestNumber(String requestNumber);
    Boolean existsByRequestNumber(String requestNumber);
    
    Long countByStatus(RequestStatus status);
    Long countByStatusNot(RequestStatus status);
    Long countByStatusIn(Collection<RequestStatus> statuses);
    Long countByStudentIdAndStatusNot(UUID studentId, RequestStatus status);
    Long countByStudentIdAndStatusIn(UUID studentId, Collection<RequestStatus> statuses);

    @Query("SELECT r FROM Request r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:category IS NULL OR LOWER(r.category) = LOWER(:category)) AND " +
           "(:assignedCell IS NULL OR LOWER(r.assignedCell) LIKE LOWER(CONCAT('%', :assignedCell, '%'))) " +
           "ORDER BY r.createdAt DESC")
    List<Request> findWithFilters(@Param("status") RequestStatus status,
                                  @Param("category") String category,
                                  @Param("assignedCell") String assignedCell);
}
