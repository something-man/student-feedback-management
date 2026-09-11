package com.college.feedback.controller;

import com.college.feedback.dto.request.ComplaintCreateRequest;
import com.college.feedback.dto.request.FeedbackResponseRequest;
import com.college.feedback.dto.request.RequestCreateRequest;
import com.college.feedback.dto.response.*;
import com.college.feedback.entity.User;
import com.college.feedback.exception.ResourceNotFoundException;
import com.college.feedback.security.UserPrincipal;
import com.college.feedback.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final AnalyticsService analyticsService;
    private final FeedbackService feedbackService;
    private final ComplaintService complaintService;
    private final RequestService requestService;
    private final NotificationService notificationService;
    private final AuthService authService;

    public StudentController(AnalyticsService analyticsService,
                             FeedbackService feedbackService,
                             ComplaintService complaintService,
                             RequestService requestService,
                             NotificationService notificationService,
                             AuthService authService) {
        this.analyticsService = analyticsService;
        this.feedbackService = feedbackService;
        this.complaintService = complaintService;
        this.requestService = requestService;
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<StudentDashboardDto>> getDashboard(@AuthenticationPrincipal UserPrincipal currentUser) {
        StudentDashboardDto dashboard = analyticsService.getStudentDashboard(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Student dashboard retrieved successfully", dashboard));
    }

    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<List<FeedbackFormDto>>> getAssignedFeedback(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<FeedbackFormDto> forms = feedbackService.getFormsAssignedToUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Assigned feedback forms retrieved successfully", forms));
    }

    @GetMapping("/feedback/{id}")
    public ResponseEntity<ApiResponse<FeedbackFormDto>> getFeedbackDetails(@PathVariable UUID id,
                                                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        FeedbackFormDto form = feedbackService.getFormDetails(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Feedback form details retrieved successfully", form));
    }

    @PostMapping("/feedback/{id}/response")
    public ResponseEntity<ApiResponse<FeedbackFormDto>> submitFeedbackResponse(@PathVariable UUID id,
                                                                               @Valid @RequestBody FeedbackResponseRequest request,
                                                                               @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = authService.getUserEntityById(currentUser.getId());
        FeedbackFormDto form = feedbackService.submitResponse(id, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Feedback response submitted successfully", form));
    }

    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<List<ComplaintDto>>> getComplaints(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<ComplaintDto> complaints = complaintService.getStudentComplaints(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Complaints retrieved successfully", complaints));
    }

    @PostMapping("/complaints")
    public ResponseEntity<ApiResponse<ComplaintDto>> createComplaint(@Valid @RequestBody ComplaintCreateRequest request,
                                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = authService.getUserEntityById(currentUser.getId());
        ComplaintDto complaint = complaintService.createComplaint(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Complaint registered successfully", complaint));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<RequestDto>>> getRequests(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<RequestDto> requests = requestService.getStudentRequests(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Requests retrieved successfully", requests));
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<RequestDto>> createRequest(@Valid @RequestBody RequestCreateRequest request,
                                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = authService.getUserEntityById(currentUser.getId());
        RequestDto reqDto = requestService.createRequest(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Request submitted successfully", reqDto));
    }

    @GetMapping("/issues/{id}")
    public ResponseEntity<ApiResponse<Object>> getIssueById(@PathVariable UUID id,
                                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            ComplaintDto complaint = complaintService.getStudentComplaintById(id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.ok("Complaint retrieved", complaint));
        } catch (ResourceNotFoundException e) {
            RequestDto req = requestService.getStudentRequestById(id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.ok("Request retrieved", req));
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved successfully", notifications));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(@PathVariable UUID id,
                                                                   @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }
}
