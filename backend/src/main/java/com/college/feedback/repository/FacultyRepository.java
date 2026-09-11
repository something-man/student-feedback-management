package com.college.feedback.repository;

import com.college.feedback.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    Optional<Faculty> findByUserId(UUID userId);
    Optional<Faculty> findByEmployeeId(String employeeId);
    Boolean existsByEmployeeId(String employeeId);
}
