package com.college.feedback;

import com.college.feedback.dto.request.*;
import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.QuestionType;
import com.college.feedback.entity.enums.TargetAudience;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@example.com", "Admin@123"))))
                .andReturn();
        JsonNode adminJson = objectMapper.readTree(adminLogin.getResponse().getContentAsString());
        adminToken = adminJson.get("data").get("token").asText();

        MvcResult studentLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("student@example.com", "Student@123"))))
                .andReturn();
        JsonNode studentJson = objectMapper.readTree(studentLogin.getResponse().getContentAsString());
        studentToken = studentJson.get("data").get("token").asText();
    }

    @Test
    void testGetAdminDashboardWithValidToken() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalResponses").isNumber())
                .andExpect(jsonPath("$.data.averageRating").isNumber());
    }

    @Test
    void testCreateFeedbackFormSuccess() throws Exception {
        FeedbackCreateRequest request = new FeedbackCreateRequest();
        request.setTitle("Mid-Semester Teaching Review");
        request.setCategory("Academic");
        request.setTargetAudience(TargetAudience.STUDENTS);
        request.setDeadline(LocalDateTime.now().plusDays(10));
        request.setAllowAnonymous(true);
        request.setQuestions(List.of(
                new QuestionCreateRequest("Course syllabus coverage quality", QuestionType.STAR_RATING, 1),
                new QuestionCreateRequest("Overall effectiveness of lecture notes", QuestionType.STAR_RATING, 2)
        ));

        mockMvc.perform(post("/api/admin/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Mid-Semester Teaching Review"))
                .andExpect(jsonPath("$.data.questions").isArray());
    }

    @Test
    void testGetAllComplaints() throws Exception {
        mockMvc.perform(get("/api/admin/complaints")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetAllRequests() throws Exception {
        mockMvc.perform(get("/api/admin/requests")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testStudentAccessingAdminRouteFails() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
