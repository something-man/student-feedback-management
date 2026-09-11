package com.college.feedback.config;

import com.college.feedback.entity.*;
import com.college.feedback.entity.enums.*;
import com.college.feedback.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final FeedbackFormRepository formRepository;
    private final FeedbackAssignmentRepository assignmentRepository;
    private final FeedbackResponseRepository responseRepository;
    private final ResponseAnswerRepository answerRepository;
    private final ComplaintRepository complaintRepository;
    private final RequestRepository requestRepository;
    private final IssueUpdateRepository issueUpdateRepository;
    private final NotificationRepository notificationRepository;
    private final AIInsightRepository aiInsightRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           FacultyRepository facultyRepository,
                           FeedbackFormRepository formRepository,
                           FeedbackAssignmentRepository assignmentRepository,
                           FeedbackResponseRepository responseRepository,
                           ResponseAnswerRepository answerRepository,
                           ComplaintRepository complaintRepository,
                           RequestRepository requestRepository,
                           IssueUpdateRepository issueUpdateRepository,
                           NotificationRepository notificationRepository,
                           AIInsightRepository aiInsightRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.formRepository = formRepository;
        this.assignmentRepository = assignmentRepository;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
        this.complaintRepository = complaintRepository;
        this.requestRepository = requestRepository;
        this.issueUpdateRepository = issueUpdateRepository;
        this.notificationRepository = notificationRepository;
        this.aiInsightRepository = aiInsightRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping initial seeding.");
            return;
        }

        log.info("Seeding initial demo data for College Feedback Management System...");

        // 1. Create Users
        User student = new User(
                "student@example.com",
                passwordEncoder.encode("Student@123"),
                "Aarav Sharma",
                Role.STUDENT,
                "CS-2024-042",
                "Computer Science",
                3
        );
        student = userRepository.save(student);

        // Student Profile Entity
        Student studentProfile = new Student(student, "CS-2024-042", "2024-2028", "B.Tech Computer Science", "Computer Science", 5);
        studentRepository.save(studentProfile);

        User faculty = new User(
                "faculty@example.com",
                passwordEncoder.encode("Faculty@123"),
                "Dr. Vikram Malhotra",
                Role.FACULTY,
                "FAC-CS-108",
                "Computer Science",
                null
        );
        faculty = userRepository.save(faculty);

        // Faculty Profile Entity
        Faculty facultyProfile = new Faculty(faculty, "FAC-CS-108", "Computer Science", "Associate Professor");
        facultyRepository.save(facultyProfile);

        User admin = new User(
                "admin@example.com",
                passwordEncoder.encode("Admin@123"),
                "Admin Office",
                Role.ADMIN,
                "ADM-001",
                "Academic Administration",
                null
        );
        admin = userRepository.save(admin);

        // 2. Create Feedback Forms & Questions
        FeedbackForm form1 = new FeedbackForm(
                "Data Science – Faculty Feedback",
                "Faculty",
                TargetAudience.STUDENTS,
                "Computer Science",
                LocalDateTime.now().plusDays(5),
                true,
                faculty
        );
        form1.addQuestion(new Question("Rate the instructor's subject knowledge & clarity of concepts.", QuestionType.STAR_RATING, true, 1));
        form1.addQuestion(new Question("How effective are the practical demonstrations and lab datasets?", QuestionType.STAR_RATING, false, 2));
        form1.addQuestion(new Question("Does the instructor encourage questions and resolve doubts effectively?", QuestionType.STAR_RATING, false, 3));
        form1.addQuestion(new Question("Any specific suggestions or constructive feedback for course improvement?", QuestionType.TEXT, false, 4));
        form1 = formRepository.save(form1);

        FeedbackForm form2 = new FeedbackForm(
                "Infrastructure & Lab Facilities Feedback",
                "Infrastructure",
                TargetAudience.STUDENTS,
                null,
                LocalDateTime.now().plusDays(8),
                true,
                admin
        );
        form2.addQuestion(new Question("Rate the condition and performance of laboratory computers & equipment.", QuestionType.STAR_RATING, true, 1));
        form2.addQuestion(new Question("Rate campus Wi-Fi network reliability and speed.", QuestionType.STAR_RATING, false, 2));
        form2.addQuestion(new Question("Classroom lighting, air conditioning, and projector facilities.", QuestionType.STAR_RATING, false, 3));
        form2 = formRepository.save(form2);

        FeedbackForm form3 = new FeedbackForm(
                "Faculty Development & Institutional Support",
                "Faculty Development",
                TargetAudience.FACULTY,
                null,
                LocalDateTime.now().plusDays(5),
                true,
                admin
        );
        form3.addQuestion(new Question("Rate the institutional research funding and conference support.", QuestionType.STAR_RATING, true, 1));
        form3.addQuestion(new Question("Adequacy of classroom teaching aids and smart boards.", QuestionType.STAR_RATING, false, 2));
        form3 = formRepository.save(form3);

        // 3. Assign Forms
        assignmentRepository.save(new FeedbackAssignment(form1, student));
        assignmentRepository.save(new FeedbackAssignment(form2, student));
        assignmentRepository.save(new FeedbackAssignment(form3, faculty));

        // 3b. Seed Sample Responses (Identified and Anonymous)
        FeedbackResponse resp1 = new FeedbackResponse(form1, student, false, 4.7);
        resp1.addAnswer(new ResponseAnswer(form1.getQuestions().get(0), 5, "Clear lecture explanations and slides."));
        resp1.addAnswer(new ResponseAnswer(form1.getQuestions().get(1), 5, "Practical datasets were very helpful."));
        resp1.addAnswer(new ResponseAnswer(form1.getQuestions().get(2), 4, "Encourages questions in class."));
        resp1.addAnswer(new ResponseAnswer(form1.getQuestions().get(3), null, "Overall very good teaching."));
        responseRepository.save(resp1);

        FeedbackResponse resp2 = new FeedbackResponse(form2, null, true, 3.7);
        resp2.addAnswer(new ResponseAnswer(form2.getQuestions().get(0), 4, "Desktops in Lab 3 are fast."));
        resp2.addAnswer(new ResponseAnswer(form2.getQuestions().get(1), 3, "Hostel Wi-Fi is slow during evenings."));
        resp2.addAnswer(new ResponseAnswer(form2.getQuestions().get(2), 4, "Classroom projector is clear."));
        responseRepository.save(resp2);

        // 4. Create Demo Complaints
        Complaint c1 = new Complaint(
                "CMP-102",
                student,
                "INFRASTRUCTURE",
                "Lab 3 AC & Projector Malfunction",
                "Projector display flickering intermittently during morning lectures in CS Lab 3, and central AC cooling is ineffective.",
                Priority.HIGH,
                IssueStatus.IN_PROGRESS
        );
        c1.setAssignedCell("Campus Facilities Cell");
        c1 = complaintRepository.save(c1);
        issueUpdateRepository.save(new IssueUpdate(IssueType.COMPLAINT, c1, null, student, "PENDING", "Complaint lodged by Aarav Sharma"));
        issueUpdateRepository.save(new IssueUpdate(IssueType.COMPLAINT, c1, null, admin, "IN_PROGRESS", "Assigned to Campus Facilities Cell. Technician dispatched."));

        Complaint c2 = new Complaint(
                "CMP-103",
                student,
                "HOSTEL",
                "Hostel Block B Water Filter Maintenance",
                "Water purifier on 2nd floor Block B has low pressure and requires cartridge replacement.",
                Priority.HIGH,
                IssueStatus.PENDING
        );
        c2 = complaintRepository.save(c2);
        issueUpdateRepository.save(new IssueUpdate(IssueType.COMPLAINT, c2, null, student, "PENDING", "Complaint lodged"));

        // 5. Create Demo Request
        Request r1 = new Request(
                "REQ-204",
                student,
                "LIBRARY",
                "IEEE Xplore Digital Library Access",
                "Requesting off-campus proxy credentials for accessing IEEE Xplore journals for final-year research project.",
                RequestStatus.RESOLVED
        );
        r1.setResolvedAt(LocalDateTime.now().minusDays(1));
        r1.setAdminNote("Proxy credentials dispatched to student email.");
        r1 = requestRepository.save(r1);
        issueUpdateRepository.save(new IssueUpdate(IssueType.REQUEST, null, r1, student, "PENDING", "Request submitted"));
        issueUpdateRepository.save(new IssueUpdate(IssueType.REQUEST, null, r1, admin, "RESOLVED", "Proxy credentials generated and activated"));

        // 6. Create Demo AI Insights
        aiInsightRepository.save(new AIInsight(
                InsightType.BOTTLENECK,
                "Wi-Fi Connectivity Surge in Hostel Block B",
                "AI sentiment analysis identified 23 recurring mentions of high latency in Hostel Block B between 8 PM - 11 PM.",
                0.94
        ));
        aiInsightRepository.save(new AIInsight(
                InsightType.TREND,
                "High Student Engagement in Data Science Lab",
                "Practical datasets received 94% positive sentiment across 120 survey responses this semester.",
                0.98
        ));
        aiInsightRepository.save(new AIInsight(
                InsightType.SLA_ALERT,
                "Facility SLA Warning: Lab AC Units",
                "3 HVAC tickets have exceeded the 48-hour resolution threshold. Escalation recommended.",
                0.89
        ));

        // 7. Create Demo Notifications
        notificationRepository.save(new Notification(
                student,
                "New Feedback Assigned",
                "Data Science – Faculty Feedback is now open for responses.",
                "FEEDBACK_ASSIGNED",
                "/feedback/" + form1.getId()
        ));
        notificationRepository.save(new Notification(
                student,
                "Complaint Update: CMP-102",
                "Technician assigned to Lab 3 AC & Projector complaint.",
                "COMPLAINT_STATUS",
                "/complaints/" + c1.getId()
        ));
        notificationRepository.save(new Notification(
                faculty,
                "New Evaluation Assigned",
                "Faculty Development & Institutional Support questionnaire has been published.",
                "FEEDBACK_ASSIGNED",
                "/feedback/" + form3.getId()
        ));

        log.info("Demo data seeding completed successfully!");
    }
}
