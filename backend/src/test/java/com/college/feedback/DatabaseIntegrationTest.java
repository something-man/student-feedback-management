package com.college.feedback;

import com.college.feedback.dto.request.*;
import com.college.feedback.dto.response.*;
import com.college.feedback.entity.*;
import com.college.feedback.entity.enums.*;
import com.college.feedback.repository.*;
import com.college.feedback.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DatabaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private FeedbackFormRepository formRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FeedbackAssignmentRepository assignmentRepository;

    @Autowired
    private FeedbackResponseRepository responseRepository;

    @Autowired
    private ResponseAnswerRepository answerRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private IssueUpdateRepository issueUpdateRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AIInsightRepository aiInsightRepository;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testStudent;
    private User testFaculty;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testStudent = userRepository.findByEmail("student@example.com")
                .orElseGet(() -> userRepository.save(new User("student@example.com", passwordEncoder.encode("Student@123"), "Aarav Sharma", Role.STUDENT, "CS-2024-042", "Computer Science", 3)));

        testFaculty = userRepository.findByEmail("faculty@example.com")
                .orElseGet(() -> userRepository.save(new User("faculty@example.com", passwordEncoder.encode("Faculty@123"), "Dr. Vikram Malhotra", Role.FACULTY, "FAC-CS-108", "Computer Science", null)));

        testAdmin = userRepository.findByEmail("admin@example.com")
                .orElseGet(() -> userRepository.save(new User("admin@example.com", passwordEncoder.encode("Admin@123"), "Admin Office", Role.ADMIN, "ADM-001", "Academic Administration", null)));
    }

    @Test
    void testStudentAndFacultyEntityPersistence() {
        Student student = studentRepository.findByUserId(testStudent.getId()).orElse(null);
        assertNotNull(student, "Student profile entity must be linked to Student user");
        assertEquals("CS-2024-042", student.getRegisterNumber());
        assertEquals("Computer Science", student.getDepartment());

        Faculty faculty = facultyRepository.findByUserId(testFaculty.getId()).orElse(null);
        assertNotNull(faculty, "Faculty profile entity must be linked to Faculty user");
        assertEquals("FAC-CS-108", faculty.getEmployeeId());
        assertEquals("Associate Professor", faculty.getDesignation());
    }

    @Test
    void testCreateFeedbackFormWithQuestionsAndAssignment() {
        FeedbackCreateRequest createReq = new FeedbackCreateRequest();
        createReq.setTitle("Semester End Lab Evaluation");
        createReq.setCategory("LAB");
        createReq.setTargetAudience(TargetAudience.STUDENTS);
        createReq.setTargetDepartment("Computer Science");
        createReq.setDeadline(LocalDateTime.now().plusDays(10));
        createReq.setAllowAnonymous(true);

        QuestionCreateRequest q1 = new QuestionCreateRequest("Rate lab hardware condition", QuestionType.STAR_RATING, 1);
        QuestionCreateRequest q2 = new QuestionCreateRequest("Any remarks for lab assistant?", QuestionType.TEXT, 2);
        createReq.setQuestions(List.of(q1, q2));

        FeedbackFormDto createdForm = feedbackService.createFeedbackForm(createReq, testAdmin);
        assertNotNull(createdForm.getId());
        assertEquals(2, createdForm.getQuestions().size());

        // Verify assignment created for student
        List<FeedbackAssignment> assignments = assignmentRepository.findByUserId(testStudent.getId());
        boolean hasAssignment = assignments.stream().anyMatch(a -> a.getForm().getId().equals(createdForm.getId()));
        assertTrue(hasAssignment, "Form should be automatically assigned to eligible student");
    }

    @Test
    void testAnonymousFeedbackPrivacyEnforcement() {
        // Create Form
        FeedbackForm form = new FeedbackForm(
                "Hostel Food Quality Survey",
                "Hostel",
                TargetAudience.STUDENTS,
                null,
                LocalDateTime.now().plusDays(7),
                true,
                testAdmin
        );
        Question q = new Question(form, "Rate hostel dinner quality", QuestionType.STAR_RATING, 1);
        form.addQuestion(q);
        form = formRepository.save(form);

        // Submit Anonymous Response
        FeedbackResponseRequest responseReq = new FeedbackResponseRequest();
        responseReq.setIsAnonymous(true);
        responseReq.setOverallRating(2.0);

        AnswerSubmitRequest ans = new AnswerSubmitRequest();
        ans.setQuestionId(q.getId());
        ans.setRatingValue(2);
        ans.setTextAnswer("Hostel food quality has deteriorated.");
        responseReq.setAnswers(List.of(ans));

        feedbackService.submitResponse(form.getId(), responseReq, testStudent);

        // Admin retrieves responses
        List<FeedbackResponseDto> responsesDto = feedbackService.getFormResponsesAsDto(form.getId());
        assertFalse(responsesDto.isEmpty());

        FeedbackResponseDto anonymousResponse = responsesDto.get(0);
        assertTrue(anonymousResponse.getIsAnonymous());
        assertNull(anonymousResponse.getUserId(), "Anonymous response MUST NOT expose userId");
        assertEquals("Anonymous Response", anonymousResponse.getUserName(), "Anonymous response MUST mask student name");
        assertNull(anonymousResponse.getUserIdentifier(), "Anonymous response MUST NOT expose student register number");
    }

    @Test
    void testComplaintLifecycleAndIssueUpdates() {
        ComplaintCreateRequest compReq = new ComplaintCreateRequest();
        compReq.setCategory("INFRASTRUCTURE");
        compReq.setSubject("Library 2nd Floor Lights Flickering");
        compReq.setDescription("Multiple tube lights are flickering in the reading section.");
        compReq.setPriority(Priority.HIGH);

        ComplaintDto created = complaintService.createComplaint(compReq, testStudent);
        assertNotNull(created.getId());
        assertTrue(created.getTicketNumber().startsWith("CMP-"));
        assertEquals(IssueStatus.PENDING, created.getStatus());

        // Update Status by Admin
        ComplaintUpdateRequest updateReq = new ComplaintUpdateRequest();
        updateReq.setStatus(IssueStatus.IN_PROGRESS);
        updateReq.setAssignedCell("Electrical Maintenance Cell");
        updateReq.setAdminNote("Electrician dispatched to library.");

        ComplaintDto updated = complaintService.updateComplaintStatus(created.getId(), updateReq, testAdmin);
        assertEquals(IssueStatus.IN_PROGRESS, updated.getStatus());
        assertEquals("Electrical Maintenance Cell", updated.getAssignedCell());

        // Verify Issue Update History
        List<IssueUpdate> updates = issueUpdateRepository.findByComplaintIdOrderByCreatedAtAsc(created.getId());
        assertTrue(updates.size() >= 2, "Should have initial log and admin update log");
        assertEquals("IN_PROGRESS", updates.get(updates.size() - 1).getNewStatus());
    }

    @Test
    void testPublicComplaintWorkflowFields() {
        Complaint complaint = new Complaint(
                "CMP-999",
                testStudent,
                "INFRASTRUCTURE",
                "Campus Main Gate Road Potholes",
                "Deep potholes near main entrance gate",
                Priority.HIGH,
                IssueStatus.PENDING
        );
        complaint.setPublicVisible(true);
        complaint.setPublicPublishedAt(LocalDateTime.now());
        complaint = complaintRepository.save(complaint);

        assertNotNull(complaint.getId());
        assertTrue(complaint.getPublicVisible());
        assertNotNull(complaint.getPublicPublishedAt());
        assertNotNull(complaint.getTargetResolutionTime());
    }

    @Test
    void testRequestLifecycleAndIssueUpdates() {
        RequestCreateRequest req = new RequestCreateRequest();
        req.setCategory("DOCUMENT");
        req.setTitle("Bonafide Certificate Request");
        req.setDetails("Required for passport renewal application.");

        RequestDto created = requestService.createRequest(req, testStudent);
        assertNotNull(created.getId());
        assertTrue(created.getRequestNumber().startsWith("REQ-"));
        assertEquals(RequestStatus.PENDING, created.getStatus());

        // Resolve by Admin
        RequestUpdateRequest updateReq = new RequestUpdateRequest();
        updateReq.setStatus(RequestStatus.RESOLVED);
        updateReq.setAdminNote("Certificate prepared and signed by Registrar.");

        RequestDto updated = requestService.updateRequestStatus(created.getId(), updateReq, testAdmin);
        assertEquals(RequestStatus.RESOLVED, updated.getStatus());

        // Check history
        List<IssueUpdate> updates = issueUpdateRepository.findByRequestIdOrderByCreatedAtAsc(created.getId());
        assertTrue(updates.size() >= 2);
    }

    @Test
    void testNotificationCreation() {
        Notification notification = new Notification(
                testStudent,
                "Exam Schedule Published",
                "Fall 2026 final exam timetable has been released.",
                "ACADEMIC_ALERT",
                "/exams"
        );
        Notification savedNotification = notificationRepository.save(notification);

        assertNotNull(savedNotification.getId());
        assertFalse(savedNotification.getIsRead());
        assertEquals("ACADEMIC_ALERT", savedNotification.getType());

        List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(testStudent.getId());
        assertTrue(userNotifications.stream().anyMatch(n -> n.getId().equals(savedNotification.getId())));
    }

    @Test
    void testAIInsightPersistence() {
        AIInsight insight = new AIInsight(
                InsightType.RECURRING_ISSUE,
                "Recurring Wi-Fi Disconnections in Block C",
                "15 complaints lodged regarding 5GHz router resets.",
                0.92
        );
        insight.setPriority("HIGH");
        AIInsight savedInsight = aiInsightRepository.save(insight);

        assertNotNull(savedInsight.getId());
        assertEquals(InsightType.RECURRING_ISSUE, savedInsight.getInsightType());
        assertEquals(0.92, savedInsight.getConfidence());

        List<AIInsight> all = aiInsightRepository.findAllByOrderByCreatedAtDesc();
        assertTrue(all.stream().anyMatch(i -> i.getId().equals(savedInsight.getId())));
    }
}
