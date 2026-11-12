package com.scms.backend.security;

import com.scms.backend.controller.ComplaintController;
import com.scms.backend.controller.UserController;
import com.scms.backend.dto.ComplaintResponse;
import com.scms.backend.exception.GlobalExceptionHandler;
import com.scms.backend.repository.DepartmentRepository;
import com.scms.backend.repository.RoleRepository;
import com.scms.backend.repository.UserRepository;
import com.scms.backend.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ComplaintController.class, UserController.class})
@Import({WebSecurityConfig.class, AuthEntryPointJwt.class, GlobalExceptionHandler.class})
@DisplayName("Security and Role-Based Access Control (RBAC) Tests")
class SecurityRBACTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComplaintService complaintService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private ComplaintResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new ComplaintResponse();
        sampleResponse.setId(1L);
        sampleResponse.setTitle("Security Test Complaint");
        sampleResponse.setStatus("Submitted");
        sampleResponse.setDateSubmitted(new Date());
    }

    @Nested
    @DisplayName("1. Unauthenticated Access Protection Tests")
    class UnauthenticatedAccessTests {

        @Test
        @DisplayName("Unauthenticated request to GET /api/complaints must return 401 Unauthorized")
        void unauthenticatedGetAllComplaintsReturns401() throws Exception {
            mockMvc.perform(get("/api/complaints"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        @Test
        @DisplayName("Unauthenticated request to GET /api/complaints/{id} must return 401 Unauthorized")
        void unauthenticatedGetComplaintByIdReturns401() throws Exception {
            mockMvc.perform(get("/api/complaints/1"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Unauthenticated request to PATCH /api/complaints/{id}/status must return 401 Unauthorized")
        void unauthenticatedUpdateStatusReturns401() throws Exception {
            mockMvc.perform(patch("/api/complaints/1/status")
                            .param("status", "In Progress"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Unauthenticated request to PATCH /api/complaints/{id}/assign must return 401 Unauthorized")
        void unauthenticatedAssignComplaintReturns401() throws Exception {
            mockMvc.perform(patch("/api/complaints/1/assign")
                            .param("staffId", "2"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Unauthenticated request to GET /api/users/staff must return 401 Unauthorized")
        void unauthenticatedGetStaffReturns401() throws Exception {
            mockMvc.perform(get("/api/users/staff")
                            .param("departmentId", "1"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    @DisplayName("2. Reporter Restriction Tests")
    class ReporterRestrictionTests {

        @Test
        @WithMockUser(username = "reporterUser", roles = {"REPORTER"})
        @DisplayName("Reporter cannot assign complaints (Expect HTTP 403 Forbidden)")
        void reporterCannotAssignComplaint() throws Exception {
            mockMvc.perform(patch("/api/complaints/1/assign")
                            .param("staffId", "20"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        @Test
        @WithMockUser(username = "reporterUser", roles = {"REPORTER"})
        @DisplayName("Reporter cannot access Coordinator-only staff endpoint (Expect HTTP 403 Forbidden)")
        void reporterCannotAccessStaffList() throws Exception {
            mockMvc.perform(get("/api/users/staff")
                            .param("departmentId", "1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        @Test
        @WithMockUser(username = "reporterUser", roles = {"REPORTER"})
        @DisplayName("Reporter cannot access another reporter's complaint (Expect HTTP 403 Forbidden)")
        void reporterCannotAccessAnotherReportersComplaint() throws Exception {
            when(complaintService.getComplaintById(2L, "reporterUser"))
                    .thenThrow(new AccessDeniedException("Access denied: You can only view your own complaints"));

            mockMvc.perform(get("/api/complaints/2"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").value("Access denied: You can only view your own complaints"));
        }
    }

    @Nested
    @DisplayName("3. Staff Restriction & Authorization Tests")
    class StaffRestrictionTests {

        @Test
        @WithMockUser(username = "staffUser", roles = {"STAFF"})
        @DisplayName("Staff cannot assign complaints (Expect HTTP 403 Forbidden)")
        void staffCannotAssignComplaint() throws Exception {
            mockMvc.perform(patch("/api/complaints/1/assign")
                            .param("staffId", "20"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        @Test
        @WithMockUser(username = "staffUser", roles = {"STAFF"})
        @DisplayName("Staff cannot access Coordinator-only staff endpoint (Expect HTTP 403 Forbidden)")
        void staffCannotAccessStaffList() throws Exception {
            mockMvc.perform(get("/api/users/staff")
                            .param("departmentId", "1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        @Test
        @WithMockUser(username = "staffUser", roles = {"STAFF"})
        @DisplayName("Staff can update complaints when assigned to them (Expect HTTP 200 OK)")
        void staffCanUpdateAssignedComplaint() throws Exception {
            sampleResponse.setStatus("In Progress");
            when(complaintService.updateComplaintStatus(eq(1L), eq("In Progress"), anyString(), eq("staffUser")))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch("/api/complaints/1/status")
                            .param("status", "In Progress"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("In Progress"));
        }

        @Test
        @WithMockUser(username = "staffUser", roles = {"STAFF"})
        @DisplayName("Staff cannot update complaints not assigned to them (Expect HTTP 403 Forbidden)")
        void staffCannotUpdateUnassignedComplaint() throws Exception {
            when(complaintService.updateComplaintStatus(eq(2L), eq("In Progress"), anyString(), eq("staffUser")))
                    .thenThrow(new AccessDeniedException("Access denied: You can only update complaints assigned to you"));

            mockMvc.perform(patch("/api/complaints/2/status")
                            .param("status", "In Progress"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").value("Access denied: You can only update complaints assigned to you"));
        }
    }

    @Nested
    @DisplayName("4. Department Coordinator Authorization & Scoping Tests")
    class CoordinatorAuthorizationTests {

        @Test
        @WithMockUser(username = "coordUser", roles = {"DEPARTMENT_COORDINATOR"})
        @DisplayName("Coordinator can assign complaints within their department (Expect HTTP 200 OK)")
        void coordinatorCanAssignComplaints() throws Exception {
            sampleResponse.setStatus("Assigned");
            when(complaintService.assignComplaint(1L, 20L, "coordUser")).thenReturn(sampleResponse);

            mockMvc.perform(patch("/api/complaints/1/assign")
                            .param("staffId", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Assigned"));
        }

        @Test
        @WithMockUser(username = "coordUser", roles = {"DEPARTMENT_COORDINATOR"})
        @DisplayName("Coordinator cannot assign complaints outside their department (Expect HTTP 403 Forbidden)")
        void coordinatorCannotAssignOutsideDepartment() throws Exception {
            when(complaintService.assignComplaint(2L, 20L, "coordUser"))
                    .thenThrow(new AccessDeniedException("Cannot assign complaint outside of your department"));

            mockMvc.perform(patch("/api/complaints/2/assign")
                            .param("staffId", "20"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").value("Cannot assign complaint outside of your department"));
        }

        @Test
        @WithMockUser(username = "coordUser", roles = {"DEPARTMENT_COORDINATOR"})
        @DisplayName("Coordinator cannot update complaints outside their department (Expect HTTP 403 Forbidden)")
        void coordinatorCannotUpdateOutsideDepartment() throws Exception {
            when(complaintService.updateComplaintStatus(eq(2L), eq("Closed"), anyString(), eq("coordUser")))
                    .thenThrow(new AccessDeniedException("Access denied: Complaint is outside your department"));

            mockMvc.perform(patch("/api/complaints/2/status")
                            .param("status", "Closed"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").value("Access denied: Complaint is outside your department"));
        }
    }
}

