package com.college.feedback;

import com.college.feedback.dto.request.*;
import com.college.feedback.entity.Complaint;
import com.college.feedback.entity.FeedbackForm;
import com.college.feedback.entity.Question;
import com.college.feedback.entity.User;
import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.Priority;
import com.college.feedback.entity.enums.Role;
import com.college.feedback.repository.ComplaintRepository;
import com.college.feedback.repository.FeedbackFormRepository;
import com.college.feedback.repository.QuestionRepository;
import com.college.feedback.repository.UserRepository;
import com.college.feedback.security.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private FeedbackFormRepository formRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String studentToken;
    private String facultyToken;
    private String adminToken;
    private User studentA;
    private User studentB;
    private String studentBToken;

    @BeforeEach
    void setup() throws Exception {
        // Ensure student B exists
        if (!userRepository.existsByEmail("student_b@example.com")) {
            studentB = new User(
                    "student_b@example.com",
                    passwordEncoder.encode("StudentB@123"),
                    "Student B",
                    Role.STUDENT,
                    "CS-2024-999",
                    "Computer Science",
                    2
            );
            studentB = userRepository.save(studentB);
        } else {
            studentB = userRepository.findByEmail("student_b@example.com").orElseThrow();
        }

        studentBToken = jwtUtils.generateTokenFromUser(studentB.getId(), studentB.getEmail(), studentB.getRole().name());

        // Login student A
        MvcResult studentLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("student@example.com", "Student@123"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode sJson = objectMapper.readTree(studentLogin.getResponse().getContentAsString());
        studentToken = sJson.get("data").get("token").asText();
        studentA = userRepository.findByEmail("student@example.com").orElseThrow();

        // Login faculty
        MvcResult facultyLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("faculty@example.com", "Faculty@123"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode fJson = objectMapper.readTree(facultyLogin.getResponse().getContentAsString());
        facultyToken = fJson.get("data").get("token").asText();

        // Login admin
        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@example.com", "Admin@123"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode aJson = objectMapper.readTree(adminLogin.getResponse().getContentAsString());
        adminToken = aJson.get("data").get("token").asText();
    }

    // =========================================================================
    // TEST 1 — LOGIN
    // =========================================================================
    @Test
    @DisplayName("TEST 1: Valid credentials return 200 OK and valid JWT token")
    void test1_ValidLoginReturnsJwt() throws Exception {
        LoginRequest req = new LoginRequest("student@example.com", "Student@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    // =========================================================================
    // TEST 2 — INVALID LOGIN
    // =========================================================================
    @Test
    @DisplayName("TEST 2: Invalid password returns 401 Unauthorized with generic message")
    void test2_InvalidPasswordReturns401() throws Exception {
        LoginRequest req = new LoginRequest("student@example.com", "IncorrectPassword123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // =========================================================================
    // TEST 3 — STUDENT ACCESS
    // =========================================================================
    @Test
    @DisplayName("TEST 3: Student JWT authorizes access to /api/student/**")
    void test3_StudentJwtAuthorizesStudentEndpoint() throws Exception {
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingFeedbackCount").isNumber());
    }

    // =========================================================================
    // TEST 4 — STUDENT ADMIN ACCESS
    // =========================================================================
    @Test
    @DisplayName("TEST 4: Student JWT attempting /api/admin/** is rejected with 403 Forbidden")
    void test4_StudentAccessingAdminEndpointReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 5 — FACULTY ADMIN ACCESS
    // =========================================================================
    @Test
    @DisplayName("TEST 5: Faculty JWT attempting /api/admin/** is rejected with 403 Forbidden")
    void test5_FacultyAccessingAdminEndpointReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 6 — UNAUTHENTICATED
    // =========================================================================
    @Test
    @DisplayName("TEST 6: Unauthenticated request to protected endpoint returns 401 Unauthorized")
    void test6_UnauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/student/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/faculty/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // TEST 7 — STUDENT OWNERSHIP (IDOR DEFENSE)
    // =========================================================================
    @Test
    @DisplayName("TEST 7: Student A cannot access Student B's complaint (IDOR Protection)")
    void test7_StudentOwnershipPreventsIDOR() throws Exception {
        // Create complaint belonging to Student B
        Complaint bComplaint = new Complaint(
                "CMP-B-999",
                studentB,
                "HOSTEL",
                "Private Issue for Student B",
                "Secret description of student B complaint",
                Priority.HIGH,
                IssueStatus.PENDING
        );
        bComplaint = complaintRepository.save(bComplaint);

        // Student B can access their own complaint
        mockMvc.perform(get("/api/student/issues/" + bComplaint.getId())
                        .header("Authorization", "Bearer " + studentBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ticketNumber").value("CMP-B-999"));

        // Student A MUST NOT be able to access Student B's complaint
        mockMvc.perform(get("/api/student/issues/" + bComplaint.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // TEST 8 — ANONYMOUS FEEDBACK PRIVACY
    // =========================================================================
    @Test
    @DisplayName("TEST 8: Anonymous feedback strictly masks student identity in Admin API")
    void test8_AnonymousFeedbackMasksStudentIdentity() throws Exception {
        FeedbackForm form = formRepository.findAll().stream().findFirst().orElseThrow();

        // Submit anonymous feedback
        FeedbackResponseRequest submitReq = new FeedbackResponseRequest();
        submitReq.setIsAnonymous(true);
        submitReq.setOverallRating(4.5);
        List<Question> questions = questionRepository.findByFormIdOrderByDisplayOrderAsc(form.getId());
        if (!questions.isEmpty()) {
            submitReq.setAnswers(List.of(
                    new AnswerSubmitRequest(questions.get(0).getId(), 5, "Anonymous comment for course")
            ));
        }

        mockMvc.perform(post("/api/student/feedback/" + form.getId() + "/response")
                        .header("Authorization", "Bearer " + studentBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isCreated());

        // Admin fetches responses
        MvcResult adminResponses = mockMvc.perform(get("/api/admin/feedback/" + form.getId() + "/responses")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responsesArray = objectMapper.readTree(adminResponses.getResponse().getContentAsString()).get("data");
        assertTrue(responsesArray.isArray());

        boolean foundAnonymous = false;
        for (JsonNode resp : responsesArray) {
            if (resp.get("isAnonymous").asBoolean()) {
                foundAnonymous = true;
                assertEquals("Anonymous Response", resp.get("userName").asText());
                assertTrue(resp.get("userId").isNull());
                assertTrue(resp.get("userIdentifier").isNull());
            }
        }
        assertTrue(foundAnonymous, "Anonymous response must exist with masked identity");
    }

    // =========================================================================
    // TEST 9 — REGISTRATION PRIVILEGE ESCALATION PREVENTION
    // =========================================================================
    @Test
    @DisplayName("TEST 9: Public self-registration attempting ADMIN role is rejected with 400 Bad Request")
    void test9_PublicAdminRegistrationIsBlocked() throws Exception {
        RegisterRequest hackerAttempt = new RegisterRequest(
                "hacker_admin@example.com",
                "Password@123",
                "Fake Admin",
                Role.ADMIN,
                "ADM-HACK",
                "Admin Office",
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hackerAttempt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Self-registration as Administrator is not permitted. Admin accounts must be created by authorized personnel."));
    }
}
