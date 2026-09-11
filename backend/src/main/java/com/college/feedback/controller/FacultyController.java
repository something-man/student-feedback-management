package com.college.feedback.controller;

import com.college.feedback.dto.request.FeedbackResponseRequest;
import com.college.feedback.dto.response.*;
import com.college.feedback.entity.User;
import com.college.feedback.security.UserPrincipal;
import com.college.feedback.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/faculty")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyController {

    private final AnalyticsService analyticsService;
    private final FeedbackService feedbackService;
    private final NotificationService notificationService;
    private final AuthService authService;

    public FacultyController(AnalyticsService analyticsService,
                             FeedbackService feedbackService,
                             NotificationService notificationService,
                             AuthService authService) {
        this.analyticsService = analyticsService;
        this.feedbackService = feedbackService;
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<FacultyDashboardDto>> getDashboard(@AuthenticationPrincipal UserPrincipal currentUser) {
        FacultyDashboardDto dashboard = analyticsService.getFacultyDashboard(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Faculty dashboard retrieved successfully", dashboard));
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
                .body(ApiResponse.ok("Faculty feedback response submitted successfully", form));
    }

    @GetMapping("/ratings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRatings(@AuthenticationPrincipal UserPrincipal currentUser) {
        Map<String, Object> ratings = analyticsService.getFacultyRatingsSummary(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Faculty ratings retrieved successfully", ratings));
    }

    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<List<AIInsightDto>>> getInsights(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<AIInsightDto> insights = analyticsService.getFacultyInsightsList(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Faculty AI insights retrieved successfully", insights));
    }

    @GetMapping("/performance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPerformance(@AuthenticationPrincipal UserPrincipal currentUser) {
        Map<String, Object> perf = analyticsService.getFacultyPerformance(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Faculty performance metrics retrieved successfully", perf));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved successfully", notifications));
    }
}
