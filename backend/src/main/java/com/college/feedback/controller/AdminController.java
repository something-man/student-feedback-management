package com.college.feedback.controller;

import com.college.feedback.dto.request.ComplaintUpdateRequest;
import com.college.feedback.dto.request.FeedbackAssignRequest;
import com.college.feedback.dto.request.FeedbackCreateRequest;
import com.college.feedback.dto.request.QuestionCreateRequest;
import com.college.feedback.dto.request.RequestUpdateRequest;
import com.college.feedback.dto.response.*;
import com.college.feedback.entity.User;
import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.Priority;
import com.college.feedback.entity.enums.RequestStatus;
import com.college.feedback.security.UserPrincipal;
import com.college.feedback.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.college.feedback.dto.request.ReportGenerateRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AnalyticsService analyticsService;
    private final FeedbackService feedbackService;
    private final ComplaintService complaintService;
    private final RequestService requestService;
    private final AuthService authService;
    private final AIService aiService;
    private final ReportService reportService;

    public AdminController(AnalyticsService analyticsService,
                           FeedbackService feedbackService,
                           ComplaintService complaintService,
                           RequestService requestService,
                           AuthService authService,
                           AIService aiService,
                           ReportService reportService) {
        this.analyticsService = analyticsService;
        this.feedbackService = feedbackService;
        this.complaintService = complaintService;
        this.requestService = requestService;
        this.authService = authService;
        this.aiService = aiService;
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboard() {
        AdminDashboardDto dashboard = analyticsService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.ok("Admin dashboard retrieved successfully", dashboard));
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<FeedbackFormDto>> createFeedbackForm(@Valid @RequestBody FeedbackCreateRequest request,
                                                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        User creator = authService.getUserEntityById(currentUser.getId());
        FeedbackFormDto form = feedbackService.createFeedbackForm(request, creator);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Feedback form created successfully", form));
    }

    @PutMapping("/feedback/{id}")
    public ResponseEntity<ApiResponse<FeedbackFormDto>> updateFeedbackForm(@PathVariable UUID id,
                                                                           @RequestBody FeedbackCreateRequest request) {
        FeedbackFormDto form = feedbackService.updateFeedbackForm(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Feedback form updated successfully", form));
    }

    @DeleteMapping("/feedback/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedbackForm(@PathVariable UUID id) {
        feedbackService.deleteFeedbackForm(id);
        return ResponseEntity.ok(ApiResponse.ok("Feedback form deleted successfully", null));
    }

    @PostMapping("/feedback/{id}/publish")
    public ResponseEntity<ApiResponse<FeedbackFormDto>> publishFeedbackForm(@PathVariable UUID id) {
        FeedbackFormDto form = feedbackService.publishFeedbackForm(id);
        return ResponseEntity.ok(ApiResponse.ok("Feedback form published successfully", form));
    }

    @PostMapping("/feedback/{id}/close")
    public ResponseEntity<ApiResponse<FeedbackFormDto>> closeFeedbackForm(@PathVariable UUID id) {
        FeedbackFormDto form = feedbackService.closeFeedbackForm(id);
        return ResponseEntity.ok(ApiResponse.ok("Feedback form closed successfully", form));
    }

    @PostMapping("/feedback/{id}/assign")
    public ResponseEntity<ApiResponse<FeedbackFormDto>> assignFeedbackForm(@PathVariable UUID id,
                                                                           @RequestBody FeedbackAssignRequest request,
                                                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        User admin = authService.getUserEntityById(currentUser.getId());
        FeedbackFormDto form = feedbackService.assignFeedbackForm(id, request, admin);
        return ResponseEntity.ok(ApiResponse.ok("Feedback form assigned successfully", form));
    }

    @PostMapping("/feedback/{id}/questions")
    public ResponseEntity<ApiResponse<QuestionDto>> addQuestionToForm(@PathVariable UUID id,
                                                                      @Valid @RequestBody QuestionCreateRequest request) {
        QuestionDto question = feedbackService.addQuestionToForm(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Question added to feedback form successfully", question));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionDto>> updateQuestion(@PathVariable UUID id,
                                                                   @RequestBody QuestionCreateRequest request) {
        QuestionDto question = feedbackService.updateQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Question updated successfully", question));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable UUID id) {
        feedbackService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.ok("Question deleted successfully", null));
    }

    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<List<FeedbackFormDto>>> getAllFeedbackForms() {
        List<FeedbackFormDto> forms = feedbackService.getAllForms();
        return ResponseEntity.ok(ApiResponse.ok("Feedback forms retrieved successfully", forms));
    }

    @GetMapping("/feedback/{id}")
    public ResponseEntity<ApiResponse<FeedbackFormDto>> getFeedbackFormById(@PathVariable UUID id) {
        FeedbackFormDto form = feedbackService.getFormDetails(id, null);
        return ResponseEntity.ok(ApiResponse.ok("Feedback form details retrieved successfully", form));
    }

    @GetMapping("/responses")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDto>>> getAllResponses(
            @RequestParam(required = false) UUID formId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<FeedbackResponseDto> responses = feedbackService.getFilteredResponses(formId, category, department, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Feedback responses retrieved successfully", responses));
    }

    @GetMapping("/feedback/{id}/responses")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDto>>> getFormResponses(@PathVariable UUID id) {
        List<FeedbackResponseDto> responses = feedbackService.getFormResponsesAsDto(id);
        return ResponseEntity.ok(ApiResponse.ok("Responses retrieved successfully", responses));
    }

    @GetMapping("/analytics/overview")
    public ResponseEntity<ApiResponse<FeedbackAnalyticsDto>> getAnalyticsOverview() {
        FeedbackAnalyticsDto analytics = analyticsService.getOverviewAnalytics();
        return ResponseEntity.ok(ApiResponse.ok("Feedback overview analytics retrieved successfully", analytics));
    }

    @GetMapping("/analytics/feedback/{id}")
    public ResponseEntity<ApiResponse<FeedbackAnalyticsDto>> getFeedbackFormAnalytics(@PathVariable UUID id) {
        FeedbackAnalyticsDto analytics = analyticsService.getFeedbackFormAnalytics(id);
        return ResponseEntity.ok(ApiResponse.ok("Feedback form analytics retrieved successfully", analytics));
    }

    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<List<ComplaintDto>>> getAllComplaints(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String assignedCell) {
        List<ComplaintDto> complaints = (status != null || priority != null || category != null || assignedCell != null)
                ? complaintService.getFilteredComplaints(status, priority, category, assignedCell)
                : complaintService.getAllComplaints();
        return ResponseEntity.ok(ApiResponse.ok("All complaints retrieved successfully", complaints));
    }

    @GetMapping("/complaints/{id}")
    public ResponseEntity<ApiResponse<ComplaintDto>> getComplaintById(@PathVariable UUID id) {
        ComplaintDto complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.ok("Complaint details retrieved successfully", complaint));
    }

    @PutMapping("/complaints/{id}")
    public ResponseEntity<ApiResponse<ComplaintDto>> updateComplaint(@PathVariable UUID id,
                                                                     @Valid @RequestBody ComplaintUpdateRequest request,
                                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        User admin = authService.getUserEntityById(currentUser.getId());
        ComplaintDto updated = complaintService.updateComplaintStatus(id, request, admin);
        return ResponseEntity.ok(ApiResponse.ok("Complaint updated successfully", updated));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<RequestDto>>> getAllRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String assignedCell) {
        List<RequestDto> requests = (status != null || category != null || assignedCell != null)
                ? requestService.getFilteredRequests(status, category, assignedCell)
                : requestService.getAllRequests();
        return ResponseEntity.ok(ApiResponse.ok("All requests retrieved successfully", requests));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<RequestDto>> getRequestById(@PathVariable UUID id) {
        RequestDto req = requestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.ok("Request details retrieved successfully", req));
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<RequestDto>> updateRequest(@PathVariable UUID id,
                                                                 @Valid @RequestBody RequestUpdateRequest request,
                                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        User admin = authService.getUserEntityById(currentUser.getId());
        RequestDto updated = requestService.updateRequestStatus(id, request, admin);
        return ResponseEntity.ok(ApiResponse.ok("Request updated successfully", updated));
    }

    // =========================================================================
    // AI ANALYTICS ENDPOINTS
    // =========================================================================

    @GetMapping({"/analytics/trend", "/analytics/feedback-trend"})
    public ResponseEntity<ApiResponse<TrendAnalyticsDto>> getFeedbackTrend(@RequestParam(defaultValue = "6m") String period) {
        TrendAnalyticsDto trend = analyticsService.getTrendAnalytics(period);
        return ResponseEntity.ok(ApiResponse.ok("Feedback trend analytics retrieved successfully", trend));
    }

    @GetMapping("/analytics/sentiment")
    public ResponseEntity<ApiResponse<SentimentAnalysisDto>> getSentimentAnalysis() {
        SentimentAnalysisDto sentiment = aiService.analyzeSentiment();
        return ResponseEntity.ok(ApiResponse.ok("Sentiment analysis generated successfully", sentiment));
    }

    @GetMapping("/analytics/recurring-issues")
    public ResponseEntity<ApiResponse<List<RecurringIssueClusterDto>>> getRecurringIssues() {
        List<RecurringIssueClusterDto> clusters = aiService.detectRecurringIssues();
        return ResponseEntity.ok(ApiResponse.ok("Recurring issue clusters detected successfully", clusters));
    }

    @GetMapping("/analytics/insights")
    public ResponseEntity<ApiResponse<List<AIInsightDto>>> getAIInsights() {
        List<AIInsightDto> insights = aiService.refreshAndGetAIInsights().stream()
                .map(AIInsightDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("AI insights retrieved successfully", insights));
    }

    @PostMapping("/analytics/insights/generate")
    public ResponseEntity<ApiResponse<List<AIInsightDto>>> regenerateAIInsights() {
        List<AIInsightDto> insights = aiService.refreshAndGetAIInsights().stream()
                .map(AIInsightDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("AI insights refreshed successfully", insights));
    }

    @GetMapping({"/analytics/complaint-status", "/analytics/complaints"})
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getComplaintStatusAnalytics() {
        java.util.Map<String, Object> stats = analyticsService.getComplaintStatusAnalytics();
        return ResponseEntity.ok(ApiResponse.ok("Complaint status distribution retrieved", stats));
    }

    @GetMapping("/analytics/complaint-priority")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getComplaintPriorityAnalytics() {
        java.util.Map<String, Object> stats = analyticsService.getComplaintPriorityAnalytics();
        return ResponseEntity.ok(ApiResponse.ok("Complaint priority distribution retrieved", stats));
    }

    @GetMapping({"/analytics/faculty-comparison", "/analytics/faculty"})
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getFacultyComparison(@RequestParam(defaultValue = "all") String dept) {
        java.util.Map<String, Object> stats = analyticsService.getFacultyComparison(dept);
        return ResponseEntity.ok(ApiResponse.ok("Faculty ratings comparison retrieved", stats));
    }

    @GetMapping("/analytics/category")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getCategoryAnalytics() {
        java.util.Map<String, Object> stats = analyticsService.getCategoryAnalytics();
        return ResponseEntity.ok(ApiResponse.ok("Category analytics retrieved successfully", stats));
    }

    @GetMapping("/analytics/ratings")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getRatingsAnalytics() {
        java.util.Map<String, Object> stats = analyticsService.getRatingsAnalytics();
        return ResponseEntity.ok(ApiResponse.ok("Ratings analytics retrieved successfully", stats));
    }


    // =========================================================================
    // REPORT GENERATION & EXPORT ENDPOINTS
    // =========================================================================

    @PostMapping("/reports/generate")
    public ResponseEntity<ApiResponse<ReportDataDto>> generateReport(@RequestBody ReportGenerateRequest request,
                                                                    @AuthenticationPrincipal UserPrincipal currentUser) {
        User admin = authService.getUserEntityById(currentUser.getId());
        ReportDataDto reportData = reportService.generateReportData(request, admin);
        return ResponseEntity.ok(ApiResponse.ok("Report generated successfully", reportData));
    }

    @GetMapping("/reports/export/pdf")
    public ResponseEntity<byte[]> exportReportPdf(@RequestParam(defaultValue = "OVERALL") String reportType,
                                                  @RequestParam(required = false) String department,
                                                  @RequestParam(required = false) String category,
                                                  @RequestParam(required = false) String term,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        User admin = authService.getUserEntityById(currentUser.getId());
        ReportGenerateRequest req = new ReportGenerateRequest(reportType, category, department, term, null, null);
        ReportDataDto reportData = reportService.generateReportData(req, admin);
        byte[] pdfBytes = reportService.exportReportToPdf(reportData);

        String filename = String.format("CFMS-Report-%s-%s.pdf", reportType.toLowerCase(), LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/reports/export/excel")
    public ResponseEntity<byte[]> exportReportExcel(@RequestParam(defaultValue = "OVERALL") String reportType,
                                                    @RequestParam(required = false) String department,
                                                    @RequestParam(required = false) String category,
                                                    @RequestParam(required = false) String term,
                                                    @AuthenticationPrincipal UserPrincipal currentUser) {
        User admin = authService.getUserEntityById(currentUser.getId());
        ReportGenerateRequest req = new ReportGenerateRequest(reportType, category, department, term, null, null);
        ReportDataDto reportData = reportService.generateReportData(req, admin);
        byte[] excelBytes = reportService.exportReportToExcel(reportData);

        String filename = String.format("CFMS-Report-%s-%s.xlsx", reportType.toLowerCase(), LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
