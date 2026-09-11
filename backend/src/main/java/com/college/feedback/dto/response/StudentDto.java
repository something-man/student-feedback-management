package com.college.feedback.dto.response;

import com.college.feedback.entity.Student;
import java.util.UUID;

public class StudentDto {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String registerNumber;
    private String batch;
    private String course;
    private String department;
    private Integer semester;

    public StudentDto() {
    }

    public static StudentDto fromEntity(Student s) {
        if (s == null) return null;
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        if (s.getUser() != null) {
            dto.setUserId(s.getUser().getId());
            dto.setFullName(s.getUser().getFullName());
            dto.setEmail(s.getUser().getEmail());
        }
        dto.setRegisterNumber(s.getRegisterNumber());
        dto.setBatch(s.getBatch());
        dto.setCourse(s.getCourse());
        dto.setDepartment(s.getDepartment());
        dto.setSemester(s.getSemester());
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

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }
}
