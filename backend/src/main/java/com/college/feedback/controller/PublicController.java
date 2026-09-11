package com.college.feedback.controller;

import com.college.feedback.dto.response.ApiResponse;
import com.college.feedback.dto.response.PublicComplaintDto;
import com.college.feedback.service.ComplaintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final ComplaintService complaintService;

    public PublicController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    /**
     * Public Complaints Feed.
     * Accessible by anyone without authentication.
     * Contains only safe public fields (zero student PII / private data).
     */
    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<List<PublicComplaintDto>>> getPublicComplaints() {
        List<PublicComplaintDto> complaints = complaintService.getPublicComplaints();
        return ResponseEntity.ok(ApiResponse.ok("Public complaints retrieved successfully", complaints));
    }
}
