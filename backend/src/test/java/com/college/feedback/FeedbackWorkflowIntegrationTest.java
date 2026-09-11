package com.college.feedback;

import com.college.feedback.dto.request.*;
import com.college.feedback.dto.response.*;
import com.college.feedback.entity.*;
import com.college.feedback.entity.enums.*;
import com.college.feedback.repository.*;
import com.college.feedback.security.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeedbackWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackFormRepository formRepository;

    @Autowired
    private FeedbackAssignmentRepository assignmentRepository;

    @Autowired
    private FeedbackResponseRepository responseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private static String adminToken;
    private static String studentToken;
    private static String facultyToken;
    private static User testAdmin;
    private static User testStudent;
    private static User testFaculty;

    private static UUID createdDraftFormId;
    private static UUID createdPublishedFormId;

    @BeforeEach
    void setupUsers() {
        if (testAdmin == null) {
            testAdmin = userRepository.findByEmail("admin@example.com").orElseGet(() ->
                    userRepository.save(new User("admin@example.com", passwordEncoder.encode("Admin@123"), "Admin Office", Role.ADMIN, "ADM-001", "Administration", null)));
            adminToken = jwtUtils.generateTokenFromUser(testAdmin.getId(), testAdmin.getEmail(), testAdmin.getRole().name());

            testStudent = userRepository.findByEmail("student@example.com").orElseGet(() ->
                    userRepository.save(new User("student@example.com", passwordEncoder.encode("Student@123"), "Aarav Sharma", Role.STUDENT, "CS-2024-042", "Computer Science", 3)));
            studentToken = jwtUtils.generateTokenFromUser(testStudent.getId(), testStudent.getEmail(), testStudent.getRole().name());

            testFaculty = userRepository.findByEmail("faculty@example.com").orElseGet(() ->
                    userRepository.save(new User("faculty@example.com", passwordEncoder.encode("Faculty@123"), "Dr. Vikram Malhotra", Role.FACULTY, "FAC-CS-108", "Computer Science", null)));
            facultyToken = jwtUtils.generateTokenFromUser(testFaculty.getId(), testFaculty.getEmail(), testFaculty.getRole().name());
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Admin creates feedback form as DRAFT with multiple question types")
    void test1_AdminCreatesFeedbackFormDraft() throws Exception {
        FeedbackCreateRequest request = new FeedbackCreateRequest();
        request.setTitle("Advanced Algorithms Course Evaluation");
        request.setDescription("Mid-semester course evaluation for CS-301");
        request.setCategory("COURSE");
        request.setStatus(FormStatus.DRAFT);
        request.setTargetAudience(TargetAudience.STUDENTS);
        request.setTargetDepartment("Computer Science");
        request.setDeadline(LocalDateTime.now().plusDays(10));
        request.setAllowAnonymous(true);

        request.setQuestions(List.of(
                new QuestionCreateRequest("How clear were the theoretical explanations?", QuestionType.RATING, true, 1),
                new QuestionCreateRequest("How effective were the practical lab sessions?", QuestionType.RATING, true, 2),
                new QuestionCreateRequest("Were all course prerequisites clearly defined?", QuestionType.YES_NO, true, 3),
                new QuestionCreateRequest("Rate overall course difficulty", QuestionType.MULTIPLE_CHOICE, false, 4),
                new QuestionCreateRequest("Any suggestions for course improvement?", QuestionType.TEXT, false, 5)
        ));

        MvcResult result = mockMvc.perform(post("/api/admin/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.title").value("Advanced Algorithms Course Evaluation"))
                .andExpect(jsonPath("$.data.questions.length()").value(5))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        createdDraftFormId = UUID.fromString(json.get("data").get("id").asText());
        assertNotNull(createdDraftFormId);
    }

    @Test
    @Order(2)
    @DisplayName("2. Student CANNOT see DRAFT forms in assigned feedback list")
    void test2_StudentCannotSeeDraftForm() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student/feedback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        for (JsonNode formNode : json) {
            assertNotEquals(createdDraftFormId.toString(), formNode.get("id").asText(),
                    "Draft form must not appear in student assigned list!");
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. Validation: Empty title rejects form creation")
    void test3_ValidationEmptyTitleRejected() throws Exception {
        FeedbackCreateRequest request = new FeedbackCreateRequest();
        request.setTitle("");
        request.setCategory("COURSE");

        mockMvc.perform(post("/api/admin/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("4. Validation: Form with NO questions cannot be published")
    void test4_ValidationNoQuestionsCannotPublish() throws Exception {
        FeedbackCreateRequest emptyFormReq = new FeedbackCreateRequest();
        emptyFormReq.setTitle("Empty Question Form");
        emptyFormReq.setCategory("GENERAL");
        emptyFormReq.setStatus(FormStatus.DRAFT);

        MvcResult createRes = mockMvc.perform(post("/api/admin/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyFormReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID emptyId = UUID.fromString(objectMapper.readTree(createRes.getResponse().getContentAsString()).get("data").get("id").asText());

        mockMvc.perform(post("/api/admin/feedback/" + emptyId + "/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Feedback form must contain at least one question before publishing"));
    }

    @Test
    @Order(5)
    @DisplayName("5. Admin publishes the DRAFT form and assigns to student")
    void test5_AdminPublishesAndAssignsForm() throws Exception {
        mockMvc.perform(post("/api/admin/feedback/" + createdDraftFormId + "/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        createdPublishedFormId = createdDraftFormId;

        // Explicit assign to test student
        FeedbackAssignRequest assignReq = new FeedbackAssignRequest();
        assignReq.setUserIds(List.of(testStudent.getId()));
        assignReq.setDeadline(LocalDateTime.now().plusDays(7));

        mockMvc.perform(post("/api/admin/feedback/" + createdPublishedFormId + "/assign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    @DisplayName("6. Student sees published assigned feedback with status PENDING")
    void test6_StudentSeesAssignedFeedbackPending() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student/feedback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        boolean found = false;
        for (JsonNode formNode : json) {
            if (formNode.get("id").asText().equals(createdPublishedFormId.toString())) {
                found = true;
                assertEquals("PENDING", formNode.get("userStatus").asText());
            }
        }
        assertTrue(found, "Assigned form should be visible to student");
    }

    @Test
    @Order(7)
    @DisplayName("7. Student opens form: status transitions to IN_PROGRESS and questions load dynamically")
    void test7_StudentOpensFormStatusTransitionsToInProgress() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student/feedback/" + createdPublishedFormId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.questions.length()").value(5))
                .andReturn();

        JsonNode form = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        assertEquals("Advanced Algorithms Course Evaluation", form.get("title").asText());
    }

    @Test
    @Order(8)
    @DisplayName("8. Validation: Missing REQUIRED question answers rejected with 400 Bad Request")
    void test8_MissingRequiredAnswersRejected() throws Exception {
        List<Question> questions = questionRepository.findByFormIdOrderByDisplayOrderAsc(createdPublishedFormId);
        assertFalse(questions.isEmpty());

        // Submit answer for only question 1 (omitting required question 2 & 3)
        FeedbackResponseRequest incompleteReq = new FeedbackResponseRequest();
        incompleteReq.setIsAnonymous(false);
        incompleteReq.setAnswers(List.of(
                new AnswerSubmitRequest(questions.get(0).getId(), 5, null)
        ));

        mockMvc.perform(post("/api/student/feedback/" + createdPublishedFormId + "/response")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(9)
    @DisplayName("9. Validation: Invalid Rating value (>5) rejected")
    void test9_InvalidRatingRejected() throws Exception {
        List<Question> questions = questionRepository.findByFormIdOrderByDisplayOrderAsc(createdPublishedFormId);

        FeedbackResponseRequest invalidRatingReq = new FeedbackResponseRequest();
        invalidRatingReq.setIsAnonymous(false);
        invalidRatingReq.setAnswers(List.of(
                new AnswerSubmitRequest(questions.get(0).getId(), 10, null), // Invalid rating 10
                new AnswerSubmitRequest(questions.get(1).getId(), 4, null),
                new AnswerSubmitRequest(questions.get(2).getId(), null, "Yes")
        ));

        mockMvc.perform(post("/api/student/feedback/" + createdPublishedFormId + "/response")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRatingReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rating values must be between 1 and 5"));
    }

    @Test
    @Order(10)
    @DisplayName("10. Student submits valid ANONYMOUS feedback: 201 Created and status COMPLETED")
    void test10_StudentSubmitsAnonymousFeedbackSuccess() throws Exception {
        List<Question> questions = questionRepository.findByFormIdOrderByDisplayOrderAsc(createdPublishedFormId);

        FeedbackResponseRequest validReq = new FeedbackResponseRequest();
        validReq.setIsAnonymous(true);
        validReq.setAnswers(List.of(
                new AnswerSubmitRequest(questions.get(0).getId(), 5, null),
                new AnswerSubmitRequest(questions.get(1).getId(), 4, null),
                new AnswerSubmitRequest(questions.get(2).getId(), null, "Yes"),
                new AnswerSubmitRequest(questions.get(3).getId(), null, "Moderate"),
                new AnswerSubmitRequest(questions.get(4).getId(), null, "More live coding demos would be great!")
        ));

        mockMvc.perform(post("/api/student/feedback/" + createdPublishedFormId + "/response")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userStatus").value("COMPLETED"));
    }

    @Test
    @Order(11)
    @DisplayName("11. Duplicate submission is strictly blocked with HTTP 409 Conflict")
    void test11_DuplicateSubmissionBlockedWith409Conflict() throws Exception {
        List<Question> questions = questionRepository.findByFormIdOrderByDisplayOrderAsc(createdPublishedFormId);

        FeedbackResponseRequest duplicateReq = new FeedbackResponseRequest();
        duplicateReq.setIsAnonymous(true);
        duplicateReq.setAnswers(List.of(
                new AnswerSubmitRequest(questions.get(0).getId(), 5, null),
                new AnswerSubmitRequest(questions.get(1).getId(), 4, null),
                new AnswerSubmitRequest(questions.get(2).getId(), null, "Yes")
        ));

        mockMvc.perform(post("/api/student/feedback/" + createdPublishedFormId + "/response")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You have already submitted this feedback."));
    }

    @Test
    @Order(12)
    @DisplayName("12. Admin Response View: Anonymous response strictly hides student identity")
    void test12_AdminResponsesStrictlyMasksAnonymousIdentity() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/feedback/" + createdPublishedFormId + "/responses")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responses = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        assertTrue(responses.isArray());
        assertFalse(responses.isEmpty());

        JsonNode resp = responses.get(0);
        assertTrue(resp.get("isAnonymous").asBoolean());
        assertTrue(resp.get("userId").isNull(), "User ID must be null for anonymous response");
        assertEquals("Anonymous Response", resp.get("userName").asText());
        assertTrue(resp.get("userIdentifier").isNull(), "User identifier must be null for anonymous response");
    }

    @Test
    @Order(13)
    @DisplayName("13. Faculty Feedback Assignment & Response workflow")
    void test13_FacultyFeedbackWorkflow() throws Exception {
        FeedbackCreateRequest facFormReq = new FeedbackCreateRequest();
        facFormReq.setTitle("Faculty Teaching Support Evaluation");
        facFormReq.setCategory("FACULTY");
        facFormReq.setStatus(FormStatus.PUBLISHED);
        facFormReq.setTargetAudience(TargetAudience.FACULTY);
        facFormReq.setDeadline(LocalDateTime.now().plusDays(5));
        facFormReq.setQuestions(List.of(
                new QuestionCreateRequest("Rate teaching resource adequacy", QuestionType.RATING, true, 1)
        ));

        MvcResult createRes = mockMvc.perform(post("/api/admin/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facFormReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID facFormId = UUID.fromString(objectMapper.readTree(createRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Faculty loads assigned feedback
        MvcResult facListRes = mockMvc.perform(get("/api/faculty/feedback")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode facList = objectMapper.readTree(facListRes.getResponse().getContentAsString()).get("data");
        assertTrue(facList.isArray());

        // Faculty loads details
        mockMvc.perform(get("/api/faculty/feedback/" + facFormId)
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk());

        // Faculty submits response
        List<Question> qList = questionRepository.findByFormIdOrderByDisplayOrderAsc(facFormId);
        FeedbackResponseRequest facRespReq = new FeedbackResponseRequest();
        facRespReq.setIsAnonymous(false);
        facRespReq.setAnswers(List.of(
                new AnswerSubmitRequest(qList.get(0).getId(), 5, "Excellent lab resources provided.")
        ));

        mockMvc.perform(post("/api/faculty/feedback/" + facFormId + "/response")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facRespReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userStatus").value("COMPLETED"));
    }

    @Test
    @Order(14)
    @DisplayName("14. Admin Overview & Per-Form Analytics return real calculated values")
    void test14_AdminAnalyticsRealCalculations() throws Exception {
        MvcResult overviewRes = mockMvc.perform(get("/api/admin/analytics/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalResponses").isNumber())
                .andExpect(jsonPath("$.data.averageRating").isNumber())
                .andExpect(jsonPath("$.data.responseRate").isNumber())
                .andReturn();

        JsonNode overview = objectMapper.readTree(overviewRes.getResponse().getContentAsString()).get("data");
        assertTrue(overview.get("totalResponses").asLong() > 0, "Total responses must be > 0");

        // Form Analytics
        mockMvc.perform(get("/api/admin/analytics/feedback/" + createdPublishedFormId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.formId").value(createdPublishedFormId.toString()))
                .andExpect(jsonPath("$.data.totalResponses").value(1))
                .andExpect(jsonPath("$.data.responseRate").isNumber());
    }

    @Test
    @Order(15)
    @DisplayName("15. Admin closes form: Subsequent student submission is blocked")
    void test15_ClosedFormSubmissionBlocked() throws Exception {
        // Admin closes form
        mockMvc.perform(post("/api/admin/feedback/" + createdPublishedFormId + "/close")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        // Second student attempting submission on closed form is blocked
        User student2 = userRepository.save(new User("student2@example.com", passwordEncoder.encode("Student@123"), "Sneha Patel", Role.STUDENT, "CS-2024-099", "Computer Science", 3));
        String student2Token = jwtUtils.generateTokenFromUser(student2.getId(), student2.getEmail(), student2.getRole().name());

        List<Question> questions = questionRepository.findByFormIdOrderByDisplayOrderAsc(createdPublishedFormId);
        FeedbackResponseRequest req = new FeedbackResponseRequest();
        req.setAnswers(List.of(new AnswerSubmitRequest(questions.get(0).getId(), 4, null)));

        mockMvc.perform(post("/api/student/feedback/" + createdPublishedFormId + "/response")
                        .header("Authorization", "Bearer " + student2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This feedback form is not currently open for responses"));
    }

    @Test
    @Order(16)
    @DisplayName("16. Validation: Blank question text in questions list is rejected")
    void test16_BlankQuestionTextRejected() throws Exception {
        FeedbackCreateRequest req = new FeedbackCreateRequest();
        req.setTitle("Feedback With Empty Question");
        req.setCategory("COURSE");
        req.setStatus(FormStatus.DRAFT);
        req.setQuestions(List.of(
                new QuestionCreateRequest("", QuestionType.RATING, true, 1)
        ));

        mockMvc.perform(post("/api/admin/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(17)
    @DisplayName("17. Validation: Student cannot submit response to FACULTY target audience form")
    void test17_StudentCannotSubmitFacultyForm() throws Exception {
        FeedbackCreateRequest facOnlyReq = new FeedbackCreateRequest();
        facOnlyReq.setTitle("Faculty Dean Survey");
        facOnlyReq.setCategory("FACULTY");
        facOnlyReq.setStatus(FormStatus.PUBLISHED);
        facOnlyReq.setTargetAudience(TargetAudience.FACULTY);
        facOnlyReq.setQuestions(List.of(new QuestionCreateRequest("Rate workload", QuestionType.RATING, true, 1)));

        MvcResult createRes = mockMvc.perform(post("/api/admin/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facOnlyReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID facOnlyFormId = UUID.fromString(objectMapper.readTree(createRes.getResponse().getContentAsString()).get("data").get("id").asText());
        List<Question> qList = questionRepository.findByFormIdOrderByDisplayOrderAsc(facOnlyFormId);

        FeedbackResponseRequest studentSubmitReq = new FeedbackResponseRequest();
        studentSubmitReq.setAnswers(List.of(new AnswerSubmitRequest(qList.get(0).getId(), 5, null)));

        mockMvc.perform(post("/api/student/feedback/" + facOnlyFormId + "/response")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentSubmitReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Students are not authorized to submit faculty feedback"));
    }

    @Test
    @Order(18)
    @DisplayName("18. Admin adds question to DRAFT form via Question API")
    void test18_AdminAddsQuestionToForm() throws Exception {
        FeedbackCreateRequest draftReq = new FeedbackCreateRequest();
        draftReq.setTitle("Draft Form for Question Tests");
        draftReq.setCategory("LIBRARY");
        draftReq.setStatus(FormStatus.DRAFT);
        draftReq.setQuestions(List.of(new QuestionCreateRequest("Initial question", QuestionType.RATING, true, 1)));

        MvcResult createRes = mockMvc.perform(post("/api/admin/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draftReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID draftId = UUID.fromString(objectMapper.readTree(createRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Add 2nd question
        QuestionCreateRequest addQReq = new QuestionCreateRequest("Library digital catalog speed", QuestionType.RATING, true, 2);
        MvcResult addRes = mockMvc.perform(post("/api/admin/feedback/" + draftId + "/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addQReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.questionText").value("Library digital catalog speed"))
                .andReturn();

        UUID q2Id = UUID.fromString(objectMapper.readTree(addRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Update 2nd question
        QuestionCreateRequest updateQReq = new QuestionCreateRequest("Updated: Library digital catalog speed & UI", QuestionType.RATING, false, 2);
        mockMvc.perform(put("/api/admin/questions/" + q2Id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateQReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionText").value("Updated: Library digital catalog speed & UI"))
                .andExpect(jsonPath("$.data.required").value(false));

        // Delete 2nd question
        mockMvc.perform(delete("/api/admin/questions/" + q2Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Delete the draft form itself
        mockMvc.perform(delete("/api/admin/feedback/" + draftId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(19)
    @DisplayName("19. Validation: Deleting feedback form with active responses is blocked")
    void test19_DeletingFormWithResponsesBlocked() throws Exception {
        mockMvc.perform(delete("/api/admin/feedback/" + createdPublishedFormId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete feedback form with active responses. Close the form instead."));
    }

    @Test
    @Order(20)
    @DisplayName("20. Admin filtered responses query with category filter")
    void test20_AdminFilteredResponses() throws Exception {
        mockMvc.perform(get("/api/admin/responses?category=COURSE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
