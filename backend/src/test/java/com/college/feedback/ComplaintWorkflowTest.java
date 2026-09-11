package com.college.feedback;

import com.college.feedback.dto.request.ComplaintCreateRequest;
import com.college.feedback.dto.request.ComplaintUpdateRequest;
import com.college.feedback.dto.request.LoginRequest;
import com.college.feedback.dto.request.RequestCreateRequest;
import com.college.feedback.dto.request.RequestUpdateRequest;
import com.college.feedback.dto.response.AuthResponse;
import com.college.feedback.dto.response.ComplaintDto;
import com.college.feedback.dto.response.PublicComplaintDto;
import com.college.feedback.dto.response.RequestDto;
import com.college.feedback.entity.User;
import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.Priority;
import com.college.feedback.entity.enums.RequestStatus;
import com.college.feedback.entity.enums.Role;
import com.college.feedback.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ComplaintWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private String student2Token;
    private String adminToken;
    private User studentUser;
    private User student2User;
    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        // Ensure student 1
        studentUser = userRepository.findByEmail("student_wf@example.com").orElseGet(() -> {
            User u = new User("student_wf@example.com", passwordEncoder.encode("Student@123"), "Aarav Sharma", Role.STUDENT, "CS-2024-042", "Computer Science", 3);
            return userRepository.save(u);
        });

        // Ensure student 2 (for privacy checks)
        student2User = userRepository.findByEmail("student_other_wf@example.com").orElseGet(() -> {
            User u = new User("student_other_wf@example.com", passwordEncoder.encode("Student@123"), "Rohan Varma", Role.STUDENT, "CS-2024-099", "Computer Science", 3);
            return userRepository.save(u);
        });

        // Ensure admin
        adminUser = userRepository.findByEmail("admin_wf@example.com").orElseGet(() -> {
            User u = new User("admin_wf@example.com", passwordEncoder.encode("Admin@123"), "Admin Office", Role.ADMIN, "ADM-001", "Administration", null);
            return userRepository.save(u);
        });

        studentToken = obtainJwtToken("student_wf@example.com", "Student@123");
        student2Token = obtainJwtToken("student_other_wf@example.com", "Student@123");
        adminToken = obtainJwtToken("admin_wf@example.com", "Admin@123");
    }

    private String obtainJwtToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("data").get("token").asText();
    }

    @Test
    @DisplayName("End-to-End Complaint Lifecycle: Create -> Assign -> In Progress -> Resolved -> Verified & Closed")
    void testFullComplaintLifecycle() throws Exception {
        // 1. Student creates a complaint
        ComplaintCreateRequest createReq = new ComplaintCreateRequest(
                "INFRASTRUCTURE",
                "Severe Lab 3 Wi-Fi Outage and Router Malfunction",
                "Entire CS Lab 3 router has dropped connection during practical lab exam.",
                Priority.HIGH
        );

        MvcResult createResult = mockMvc.perform(post("/api/student/complaints")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.publicVisible").value(true))
                .andReturn();

        JsonNode createNode = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data");
        String complaintId = createNode.get("id").asText();
        String ticketNumber = createNode.get("ticketNumber").asText();

        // 2. Student tracks own issue and verifies initial IssueUpdate audit timeline
        MvcResult trackResult = mockMvc.perform(get("/api/student/issues/" + complaintId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updates").isArray())
                .andExpect(jsonPath("$.data.updates[0].statusUpdate").value("PENDING"))
                .andReturn();

        // 3. Security check: Student 2 CANNOT access Student 1's complaint
        mockMvc.perform(get("/api/student/issues/" + complaintId)
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isNotFound());

        // 4. Admin reviews all complaints and filters by INFRASTRUCTURE
        mockMvc.perform(get("/api/admin/complaints?category=INFRASTRUCTURE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 5. Admin assigns complaint to IT Department and updates status to IN_PROGRESS
        ComplaintUpdateRequest assignReq = new ComplaintUpdateRequest(
                IssueStatus.IN_PROGRESS,
                "IT Department",
                "Assigned to Network Operations Team. Dispatching technician."
        );
        assignReq.setPriority(Priority.HIGH);

        mockMvc.perform(put("/api/admin/complaints/" + complaintId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.assignedCell").value("IT Department"))
                .andExpect(jsonPath("$.data.updates.length()").value(2));

        // 6. Public Portal check: High priority unresolved complaint is visible on public API with NO student PII
        MvcResult publicResult = mockMvc.perform(get("/api/public/complaints"))
                .andExpect(status().isOk())
                .andReturn();

        String publicJson = publicResult.getResponse().getContentAsString();
        assertThat(publicJson).contains(ticketNumber);
        assertThat(publicJson).doesNotContain("student_wf@example.com");
        assertThat(publicJson).doesNotContain("Aarav Sharma");
        assertThat(publicJson).doesNotContain("CS-2024-042");

        // 7. Responsible Cell marks complaint as RESOLVED
        ComplaintUpdateRequest resolveReq = new ComplaintUpdateRequest(
                IssueStatus.RESOLVED,
                "IT Department",
                "New dual-band Wi-Fi 6 access point installed and signal verified."
        );

        mockMvc.perform(put("/api/admin/complaints/" + complaintId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolvedAt").isNotEmpty());

        // 8. Admin verifies resolution and closes complaint (RESOLVED -> CLOSED)
        ComplaintUpdateRequest closeReq = new ComplaintUpdateRequest(
                IssueStatus.CLOSED,
                "IT Department",
                "Resolution inspected and confirmed. Ticket officially closed."
        );

        mockMvc.perform(put("/api/admin/complaints/" + complaintId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.verifiedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.publicVisible").value(false));

        // 9. Verify Closed complaint is removed from active Public Portal feed
        MvcResult publicAfterClose = mockMvc.perform(get("/api/public/complaints"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(publicAfterClose.getResponse().getContentAsString()).doesNotContain(ticketNumber);

        // 10. Student checks tracking timeline after closure: Full audit history is retained!
        MvcResult finalTrackResult = mockMvc.perform(get("/api/student/issues/" + complaintId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.updates.length()").value(4))
                .andReturn();

        JsonNode finalNode = objectMapper.readTree(finalTrackResult.getResponse().getContentAsString()).get("data");
        assertThat(finalNode.get("updates").size()).isEqualTo(4);
    }

    @Test
    @DisplayName("End-to-End Request Lifecycle: Create -> Assign -> In Progress -> Approved -> Completed")
    void testFullRequestLifecycle() throws Exception {
        // 1. Student creates a service request
        RequestCreateRequest req = new RequestCreateRequest(
                "DOCUMENT",
                "Bonafide Certificate for National Internship",
                "Required for applying to DRDO Summer Research Fellowship 2026."
        );

        MvcResult createResult = mockMvc.perform(post("/api/student/requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.requestNumber").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        JsonNode reqNode = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data");
        String requestId = reqNode.get("id").asText();

        // 2. Admin updates request to APPROVED & assigns to Academic Office
        RequestUpdateRequest approveReq = new RequestUpdateRequest(
                RequestStatus.APPROVED,
                null,
                "Academic Office",
                "Bonafide certificate approved and queued for digital stamping."
        );

        mockMvc.perform(put("/api/admin/requests/" + requestId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.assignedCell").value("Academic Office"));

        // 3. Admin completes the request
        RequestUpdateRequest completeReq = new RequestUpdateRequest(
                RequestStatus.COMPLETED,
                null,
                "Academic Office",
                "Signed and stamped PDF dispatched to student portal and email."
        );

        mockMvc.perform(put("/api/admin/requests/" + requestId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.resolvedAt").isNotEmpty());

        // 4. Student views tracking timeline
        mockMvc.perform(get("/api/student/issues/" + requestId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.updates.length()").value(3));
    }
}
