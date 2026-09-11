package com.college.feedback.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Table(name = "faculties", indexes = {
        @Index(name = "idx_faculty_emp_id", columnList = "employeeId")
})
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String employeeId;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String designation; // e.g. "Associate Professor"

    public Faculty() {
    }

    public Faculty(User user, String employeeId, String department, String designation) {
        this.user = user;
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
