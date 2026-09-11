package com.college.feedback.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_reg_no", columnList = "registerNumber")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String registerNumber;

    @Column(length = 50)
    private String batch; // e.g. "2024-2028"

    @Column(length = 100)
    private String course; // e.g. "B.Tech Computer Science"

    @Column(length = 100)
    private String department;

    private Integer semester; // e.g. 5

    public Student() {
    }

    public Student(User user, String registerNumber, String batch, String course, String department, Integer semester) {
        this.user = user;
        this.registerNumber = registerNumber;
        this.batch = batch;
        this.course = course;
        this.department = department;
        this.semester = semester;
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
