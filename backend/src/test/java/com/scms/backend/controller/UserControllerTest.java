package com.scms.backend.controller;

import com.scms.backend.entity.Department;
import com.scms.backend.entity.Role;
import com.scms.backend.entity.User;
import com.scms.backend.exception.GlobalExceptionHandler;
import com.scms.backend.repository.DepartmentRepository;
import com.scms.backend.repository.RoleRepository;
import com.scms.backend.repository.UserRepository;
import com.scms.backend.security.AuthEntryPointJwt;
import com.scms.backend.security.JwtUtils;
import com.scms.backend.security.UserDetailsServiceImpl;
import com.scms.backend.security.WebSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({WebSecurityConfig.class, AuthEntryPointJwt.class, GlobalExceptionHandler.class})
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    @WithMockUser(username = "coordIT", roles = {"DEPARTMENT_COORDINATOR"})
    @DisplayName("Coordinator can fetch staff members by department ID")
    void coordinatorCanFetchStaffByDepartment() throws Exception {
        Role staffRole = new Role(2L, "STAFF");
        Department deptIT = new Department(1L, "IT");
        User staff = new User(20L, "staff1", "staff1@test.com", "pass", "12345", staffRole, deptIT, new Date());

        when(userRepository.findByDepartmentIdAndRoleName(1L, "STAFF")).thenReturn(List.of(staff));

        mockMvc.perform(get("/api/users/staff")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("staff1"))
                .andExpect(jsonPath("$[0].role").value("STAFF"));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    @DisplayName("Admin can fetch all system users")
    void adminCanFetchAllUsers() throws Exception {
        Role reporterRole = new Role(1L, "REPORTER");
        User user = new User(10L, "reporter1", "rep@test.com", "pass", "123", reporterRole, null, new Date());

        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("reporter1"));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    @DisplayName("Admin can assign privileged role to a user")
    void adminCanUpdateUserRole() throws Exception {
        Role reporterRole = new Role(1L, "REPORTER");
        Role staffRole = new Role(2L, "STAFF");
        Department deptIT = new Department(1L, "IT");
        User user = new User(10L, "john", "john@test.com", "pass", "123", reporterRole, null, new Date());

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("STAFF")).thenReturn(Optional.of(staffRole));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(deptIT));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/users/10/role")
                        .param("role", "STAFF")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.departmentId").value(1));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    @DisplayName("Should return 404 Not Found when updating role of non-existent user")
    void shouldReturn404WhenUserNotFoundForRoleUpdate() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/999/role")
                        .param("role", "STAFF"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}

