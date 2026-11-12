package com.scms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scms.backend.dto.ComplaintRequest;
import com.scms.backend.dto.ComplaintResponse;
import com.scms.backend.exception.GlobalExceptionHandler;
import com.scms.backend.exception.ResourceNotFoundException;
import com.scms.backend.security.AuthEntryPointJwt;
import com.scms.backend.security.JwtUtils;
import com.scms.backend.security.UserDetailsServiceImpl;
import com.scms.backend.security.WebSecurityConfig;
import com.scms.backend.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComplaintController.class)
@Import({WebSecurityConfig.class, AuthEntryPointJwt.class, GlobalExceptionHandler.class})
@DisplayName("ComplaintController Integration Tests")
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComplaintService complaintService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private ComplaintResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new ComplaintResponse();
        sampleResponse.setId(1L);
        sampleResponse.setTitle("Broken Air Conditioner");
        sampleResponse.setDescription("AC leaking water in Room 201.");
        sampleResponse.setCategory("HVAC");
        sampleResponse.setPriority("High");
        sampleResponse.setStatus("Submitted");
        sampleResponse.setDateSubmitted(new Date());
        sampleResponse.setUserEmail("reporter@example.com");
        sampleResponse.setDepartmentName("Civil");
        sampleResponse.setRoomNumber("201");
    }

    @Nested
    @DisplayName("Complaint Creation Endpoint Tests")
    class CreateComplaintTests {

        @Test
        @WithMockUser(username = "reporter1", roles = {"REPORTER"})
        @DisplayName("Should create complaint and return 200 OK with ComplaintResponse")
        void shouldCreateComplaintSuccessfully() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "images", "photo.jpg", "image/jpeg", "image-content".getBytes());

            when(complaintService.createComplaint(any(ComplaintRequest.class), eq("reporter1")))
                    .thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/complaints")
                            .file(file)
                            .param("title", "Broken Air Conditioner")
                            .param("description", "AC leaking water in Room 201.")
                            .param("category", "HVAC")
                            .param("priority", "High")
                            .param("departmentId", "1")
                            .param("roomNumber", "201"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Broken Air Conditioner"))
                    .andExpect(jsonPath("$.status").value("Submitted"))
                    .andExpect(jsonPath("$.departmentName").value("Civil"));

            verify(complaintService).createComplaint(any(ComplaintRequest.class), eq("reporter1"));
        }

        @Test
        @WithMockUser(username = "reporter1", roles = {"REPORTER"})
        @DisplayName("Should return 400 Bad Request when service rejects missing required fields")
        void shouldReturn400OnInvalidComplaintInput() throws Exception {
            when(complaintService.createComplaint(any(ComplaintRequest.class), eq("reporter1")))
                    .thenThrow(new IllegalArgumentException("Title is required"));

            mockMvc.perform(multipart("/api/complaints")
                            .param("description", "Missing title")
                            .param("category", "HVAC")
                            .param("priority", "High")
                            .param("departmentId", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Title is required"));
        }
    }

    @Nested
    @DisplayName("Complaint Retrieval Endpoints Tests")
    class GetComplaintTests {

        @Test
        @WithMockUser(username = "reporter1", roles = {"REPORTER"})
        @DisplayName("Should return 200 OK with list of complaints")
        void shouldGetAllComplaints() throws Exception {
            when(complaintService.getAllComplaints("reporter1")).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/complaints"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Broken Air Conditioner"));
        }

        @Test
        @WithMockUser(username = "reporter1", roles = {"REPORTER"})
        @DisplayName("Should return 200 OK with complaint details by ID")
        void shouldGetComplaintById() throws Exception {
            when(complaintService.getComplaintById(1L, "reporter1")).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/complaints/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Broken Air Conditioner"));
        }

        @Test
        @WithMockUser(username = "reporter1", roles = {"REPORTER"})
        @DisplayName("Should return 404 Not Found for non-existent complaint ID")
        void shouldReturn404WhenComplaintNotFound() throws Exception {
            when(complaintService.getComplaintById(999L, "reporter1"))
                    .thenThrow(new ResourceNotFoundException("Complaint not found with id: 999"));

            mockMvc.perform(get("/api/complaints/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Complaint not found with id: 999"));
        }
    }

    @Nested
    @DisplayName("Complaint Assignment Endpoint Tests")
    class AssignComplaintTests {

        @Test
        @WithMockUser(username = "coordIT", roles = {"DEPARTMENT_COORDINATOR"})
        @DisplayName("Coordinator can assign complaint to staff")
        void shouldAssignComplaintSuccessfully() throws Exception {
            sampleResponse.setStatus("Assigned");
            sampleResponse.setAssignedStaffEmail("staff@test.com");

            when(complaintService.assignComplaint(1L, 20L, "coordIT")).thenReturn(sampleResponse);

            mockMvc.perform(patch("/api/complaints/1/assign")
                            .param("staffId", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("Assigned"))
                    .andExpect(jsonPath("$.assignedStaffEmail").value("staff@test.com"));
        }

        @Test
        @WithMockUser(username = "coordIT", roles = {"DEPARTMENT_COORDINATOR"})
        @DisplayName("Should return 400 Bad Request when assigning to staff of a different department")
        void shouldReturn400WhenAssigningStaffInDifferentDepartment() throws Exception {
            when(complaintService.assignComplaint(1L, 99L, "coordIT"))
                    .thenThrow(new IllegalArgumentException("Staff member does not belong to the department responsible for this complaint"));

            mockMvc.perform(patch("/api/complaints/1/assign")
                            .param("staffId", "99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Staff member does not belong to the department responsible for this complaint"));
        }
    }

    @Nested
    @DisplayName("Complaint Status Update Endpoint Tests")
    class UpdateStatusTests {

        @Test
        @WithMockUser(username = "staffIT", roles = {"STAFF"})
        @DisplayName("Staff can update complaint status with remarks")
        void shouldUpdateComplaintStatusSuccessfully() throws Exception {
            sampleResponse.setStatus("In Progress");

            when(complaintService.updateComplaintStatus(1L, "In Progress", "Parts ordered", "staffIT"))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch("/api/complaints/1/status")
                            .param("status", "In Progress")
                            .param("remarks", "Parts ordered"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("In Progress"));
        }

        @Test
        @WithMockUser(username = "staffIT", roles = {"STAFF"})
        @DisplayName("Should return 400 Bad Request for unrecognized status transition")
        void shouldReturn400OnInvalidStatus() throws Exception {
            when(complaintService.updateComplaintStatus(eq(1L), eq("BogusStatus"), anyString(), eq("staffIT")))
                    .thenThrow(new IllegalArgumentException("Invalid status: BogusStatus"));

            mockMvc.perform(patch("/api/complaints/1/status")
                            .param("status", "BogusStatus"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid status: BogusStatus"));
        }
    }
}

