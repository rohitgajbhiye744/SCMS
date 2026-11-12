package com.scms.backend.controller;

import com.scms.backend.entity.Department;
import com.scms.backend.exception.GlobalExceptionHandler;
import com.scms.backend.repository.DepartmentRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
@Import({WebSecurityConfig.class, AuthEntryPointJwt.class, GlobalExceptionHandler.class})
@DisplayName("DepartmentController Integration Tests")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Public endpoint: Should return all departments without authentication")
    void shouldReturnAllDepartmentsPublicly() throws Exception {
        Department dept1 = new Department(1L, "IT");
        Department dept2 = new Department(2L, "Computer Engineering");

        when(departmentRepository.findAll()).thenReturn(List.of(dept1, dept2));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("IT"))
                .andExpect(jsonPath("$[1].name").value("Computer Engineering"));
    }
}

