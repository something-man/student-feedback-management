package com.college.feedback;

import com.college.feedback.dto.request.ComplaintCreateRequest;
import com.college.feedback.dto.request.LoginRequest;
import com.college.feedback.dto.request.RequestCreateRequest;
import com.college.feedback.entity.enums.Priority;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String studentToken;
    private String facultyToken;

    @BeforeEach
    void setUp() throws Exception {
        // Login student
        MvcResult studentLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("student@example.com", "Student@123"))))
                .andReturn();
        JsonNode studentJson = objectMapper.readTree(studentLogin.getResponse().getContentAsString());
        studentToken = studentJson.get("data").get("token").asText();

        // Login faculty
        MvcResult facultyLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("faculty@example.com", "Faculty@123"))))
                .andReturn();
        JsonNode facultyJson = objectMapper.readTree(facultyLogin.getResponse().getContentAsString());
        facultyToken = facultyJson.get("data").get("token").asText();
    }

    @Test
    void testGetStudentDashboardWithValidToken() throws Exception {
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingFeedbackCount").isNumber())
                .andExpect(jsonPath("$.data.activeComplaintsCount").isNumber());
    }

    @Test
    void testGetStudentFeedbackList() throws Exception {
        mockMvc.perform(get("/api/student/feedback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testCreateComplaintSuccess() throws Exception {
        ComplaintCreateRequest request = new ComplaintCreateRequest(
                "INFRASTRUCTURE",
                "Broken Desk in Room 402",
                "The desk in front row room 402 has a loose hinge.",
                Priority.LOW
        );

        mockMvc.perform(post("/api/student/complaints")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.subject").value("Broken Desk in Room 402"));
    }

    @Test
    void testCreateRequestSuccess() throws Exception {
        RequestCreateRequest request = new RequestCreateRequest(
                "LAB",
                "GPU Server Access for ML Project",
                "Need access to the departmental RTX 4090 GPU cluster for thesis training."
        );

        mockMvc.perform(post("/api/student/requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.title").value("GPU Server Access for ML Project"));
    }

    @Test
    void testStudentEndpointWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/student/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testFacultyAccessingStudentRouteFails() throws Exception {
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden());
    }
}
