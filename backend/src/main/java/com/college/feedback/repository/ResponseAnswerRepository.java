package com.college.feedback.repository;

import com.college.feedback.entity.ResponseAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResponseAnswerRepository extends JpaRepository<ResponseAnswer, UUID> {
    List<ResponseAnswer> findByResponseId(UUID responseId);
    List<ResponseAnswer> findByQuestionId(UUID questionId);
}
