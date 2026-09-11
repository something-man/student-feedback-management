package com.college.feedback;

import com.college.feedback.dto.request.ReportGenerateRequest;
import com.college.feedback.dto.response.RecurringIssueClusterDto;
import com.college.feedback.dto.response.ReportDataDto;
import com.college.feedback.dto.response.SentimentAnalysisDto;
import com.college.feedback.entity.Complaint;
import com.college.feedback.entity.Notification;
import com.college.feedback.entity.User;
import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.Priority;
import com.college.feedback.entity.enums.Role;
import com.college.feedback.repository.ComplaintRepository;
import com.college.feedback.repository.NotificationRepository;
import com.college.feedback.repository.UserRepository;
import com.college.feedback.service.AIService;
import com.college.feedback.service.NotificationService;
import com.college.feedback.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReportAndAIIntegrationTest {

    @Autowired
    private AIService aiService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testAdmin;
    private User testStudent;

    @BeforeEach
    void setUp() {
        testAdmin = userRepository.findByEmail("admin@example.com")
                .orElseGet(() -> userRepository.save(new User("admin_test@example.com", "pass", "Admin User", Role.ADMIN, "ADM-999", "Admin", null)));

        testStudent = userRepository.findByEmail("student@example.com")
                .orElseGet(() -> userRepository.save(new User("student_test@example.com", "pass", "Student User", Role.STUDENT, "STU-999", "CS", 4)));
    }

    @Test
    @DisplayName("AI Sentiment Analysis generates positive, neutral, negative breakdown")
    void testAnalyzeSentiment() {
        SentimentAnalysisDto sentiment = aiService.analyzeSentiment();
        assertNotNull(sentiment);
        assertNotNull(sentiment.getPositivePercent());
        assertNotNull(sentiment.getNeutralPercent());
        assertNotNull(sentiment.getNegativePercent());
        assertTrue(sentiment.getPositivePercent() >= 0.0 && sentiment.getPositivePercent() <= 100.0);
        assertNotNull(sentiment.getSummary());
        assertFalse(sentiment.getSummary().isBlank());
    }

    @Test
    @DisplayName("AI Recurring Complaint Detection identifies distinct problem clusters")
    void testDetectRecurringIssues() {
        List<RecurringIssueClusterDto> clusters = aiService.detectRecurringIssues();
        assertNotNull(clusters);
        assertFalse(clusters.isEmpty());

        for (RecurringIssueClusterDto cluster : clusters) {
            assertNotNull(cluster.getClusterId());
            assertNotNull(cluster.getTitle());
            assertNotNull(cluster.getCategory());
            assertNotNull(cluster.getCount());
            assertTrue(cluster.getCount() > 0);
        }
    }

    @Test
    @DisplayName("AI Priority Scoring calculates weighted score between 0 and 100")
    void testPriorityScoreCalculation() {
        Complaint criticalComplaint = new Complaint(
                "TICK-TEST-001",
                testStudent,
                "INFRASTRUCTURE",
                "Fire hazard and severe electrical shock risk in CS Lab",
                "Electric short circuit near circuit breaker with smoke emergency.",
                Priority.CRITICAL,
                IssueStatus.PENDING
        );

        int score = aiService.calculatePriorityScore(criticalComplaint);
        assertTrue(score >= 80, "Critical emergency complaint should score at least 80, got: " + score);

        Complaint lowComplaint = new Complaint(
                "TICK-TEST-002",
                testStudent,
                "GENERAL",
                "Request for extra waste bins in canteen garden",
                "Could we place additional recycling bins near the outdoor seating benches.",
                Priority.LOW,
                IssueStatus.PENDING
        );

        int lowScore = aiService.calculatePriorityScore(lowComplaint);
        assertTrue(lowScore < score, "Low priority complaint should have lower score than critical emergency");
    }

    @Test
    @DisplayName("Report Service generates all 7 report data models correctly")
    void testGenerateAllReportTypes() {
        String[] reportTypes = {"OVERALL", "FACULTY", "COURSE", "INFRASTRUCTURE", "COMPLAINT", "REQUEST", "AI_INSIGHTS"};

        for (String type : reportTypes) {
            ReportGenerateRequest req = new ReportGenerateRequest(type, null, null, "Fall 2026", null, null);
            ReportDataDto report = reportService.generateReportData(req, testAdmin);

            assertNotNull(report, "Report should not be null for type: " + type);
            assertEquals(type, report.getReportType());
            assertNotNull(report.getTitle());
            assertNotNull(report.getHeaders());
            assertFalse(report.getHeaders().isEmpty());
            assertNotNull(report.getRows());
            assertNotNull(report.getSummaryMetrics());
        }
    }

    @Test
    @DisplayName("Report Service exports valid PDF binary streams")
    void testExportPdf() {
        ReportGenerateRequest req = new ReportGenerateRequest("OVERALL", null, null, null, null, null);
        ReportDataDto reportData = reportService.generateReportData(req, testAdmin);
        byte[] pdf = reportService.exportReportToPdf(reportData);

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF should contain valid binary payload");
        // Check PDF magic bytes '%PDF'
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    @DisplayName("Report Service exports valid Excel (.xlsx) binary streams")
    void testExportExcel() {
        ReportGenerateRequest req = new ReportGenerateRequest("FACULTY", null, null, null, null, null);
        ReportDataDto reportData = reportService.generateReportData(req, testAdmin);
        byte[] xlsx = reportService.exportReportToExcel(reportData);

        assertNotNull(xlsx);
        assertTrue(xlsx.length > 500, "Excel export should contain valid binary payload");
        // Check ZIP magic bytes 'PK' (0x50, 0x4B)
        assertEquals('P', (char) xlsx[0]);
        assertEquals('K', (char) xlsx[1]);
    }

    @Test
    @DisplayName("Notification Service marks all user notifications as read")
    void testMarkAllNotificationsAsRead() {
        notificationRepository.save(new Notification(testStudent, "Alert 1", "Message 1", "INFO", null));
        notificationRepository.save(new Notification(testStudent, "Alert 2", "Message 2", "INFO", null));

        long unreadBefore = notificationService.getUnreadCount(testStudent.getId());
        assertTrue(unreadBefore >= 2);

        notificationService.markAllAsRead(testStudent.getId());

        long unreadAfter = notificationService.getUnreadCount(testStudent.getId());
        assertEquals(0, unreadAfter, "All notifications should be marked as read");
    }
}
