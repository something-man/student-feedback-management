package com.college.feedback.dto.response;

import com.college.feedback.entity.Faculty;
import java.util.UUID;

public class FacultyDto {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String employeeId;
    private String department;
    private String designation;

    public FacultyDto() {
    }

    public static FacultyDto fromEntity(Faculty f) {
        if (f == null) return null;
        FacultyDto dto = new FacultyDto();
        dto.setId(f.getId());
        if (f.getUser() != null) {
            dto.setUserId(f.getUser().getId());
            dto.setFullName(f.getUser().getFullName());
            dto.setEmail(f.getUser().getEmail());
        }
        dto.setEmployeeId(f.getEmployeeId());
        dto.setDepartment(f.getDepartment());
        dto.setDesignation(f.getDesignation());
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
