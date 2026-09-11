package com.college.feedback;

import com.college.feedback.dto.request.LoginRequest;
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
class FacultyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String facultyToken;
    private String studentToken;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult facultyLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("faculty@example.com", "Faculty@123"))))
                .andReturn();
        JsonNode facultyJson = objectMapper.readTree(facultyLogin.getResponse().getContentAsString());
        facultyToken = facultyJson.get("data").get("token").asText();

        MvcResult studentLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("student@example.com", "Student@123"))))
                .andReturn();
        JsonNode studentJson = objectMapper.readTree(studentLogin.getResponse().getContentAsString());
        studentToken = studentJson.get("data").get("token").asText();
    }

    @Test
    void testGetFacultyDashboardWithValidToken() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.averageRating").isNumber())
                .andExpect(jsonPath("$.data.studentResponseRate").isNumber());
    }

    @Test
    void testGetFacultyRatings() throws Exception {
        mockMvc.perform(get("/api/faculty/ratings")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallRating").isNotEmpty());
    }

    @Test
    void testGetFacultyInsights() throws Exception {
        mockMvc.perform(get("/api/faculty/insights")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testStudentAccessingFacultyRouteFails() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
