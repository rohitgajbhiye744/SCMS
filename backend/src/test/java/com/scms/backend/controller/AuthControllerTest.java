package com.scms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scms.backend.dto.LoginRequest;
import com.scms.backend.dto.SignupRequest;
import com.scms.backend.entity.Department;
import com.scms.backend.entity.Role;
import com.scms.backend.entity.User;
import com.scms.backend.exception.GlobalExceptionHandler;
import com.scms.backend.repository.DepartmentRepository;
import com.scms.backend.repository.RoleRepository;
import com.scms.backend.repository.UserRepository;
import com.scms.backend.security.AuthEntryPointJwt;
import com.scms.backend.security.JwtUtils;
import com.scms.backend.security.UserDetailsImpl;
import com.scms.backend.security.UserDetailsServiceImpl;
import com.scms.backend.security.WebSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({WebSecurityConfig.class, AuthEntryPointJwt.class, GlobalExceptionHandler.class})
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private PasswordEncoder encoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private Role roleReporter;
    private Department department;

    @BeforeEach
    void setUp() {
        roleReporter = new Role(1L, "REPORTER");
        department = new Department(1L, "IT");
    }

    @Nested
    @DisplayName("User Registration Endpoint Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should successfully register a new user with safe default role REPORTER")
        void shouldRegisterNewUserWithDefaultRole() throws Exception {
            // Arrange
            SignupRequest request = new SignupRequest();
            request.setUsername("newreporter");
            request.setEmail("reporter@example.com");
            request.setPassword("password123");
            request.setPhone("9876543210");
            request.setDepartmentId(1L);

            when(userRepository.existsByUsername("newreporter")).thenReturn(false);
            when(userRepository.existsByEmail("reporter@example.com")).thenReturn(false);
            when(roleRepository.findByName("REPORTER")).thenReturn(Optional.of(roleReporter));
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            when(encoder.encode("password123")).thenReturn("encodedPassword123");

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User registered successfully!"));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getUsername()).isEqualTo("newreporter");
            assertThat(savedUser.getEmail()).isEqualTo("reporter@example.com");
            assertThat(savedUser.getRole().getName()).isEqualTo("REPORTER");
        }

        @Test
        @DisplayName("Issue 1: Public registration with requested privileged role STAFF must still assign REPORTER")
        void shouldPreventRoleEscalationToStaff() throws Exception {
            // Arrange
            SignupRequest request = new SignupRequest();
            request.setUsername("attacker");
            request.setEmail("attacker@example.com");
            request.setPassword("password123");
            request.setRole("STAFF"); // Attempt privilege escalation

            when(userRepository.existsByUsername("attacker")).thenReturn(false);
            when(userRepository.existsByEmail("attacker@example.com")).thenReturn(false);
            when(roleRepository.findByName("REPORTER")).thenReturn(Optional.of(roleReporter));
            when(encoder.encode(any())).thenReturn("encodedPassword");

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User registered successfully!"));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            // Safe default role REPORTER must be assigned, ignoring requested STAFF role
            assertThat(savedUser.getRole().getName()).isEqualTo("REPORTER");
        }

        @Test
        @DisplayName("Issue 1: Public registration with requested privileged role DEPARTMENT_COORDINATOR must still assign REPORTER")
        void shouldPreventRoleEscalationToCoordinator() throws Exception {
            // Arrange
            SignupRequest request = new SignupRequest();
            request.setUsername("attacker2");
            request.setEmail("attacker2@example.com");
            request.setPassword("password123");
            request.setRole("DEPARTMENT_COORDINATOR"); // Attempt privilege escalation

            when(userRepository.existsByUsername("attacker2")).thenReturn(false);
            when(userRepository.existsByEmail("attacker2@example.com")).thenReturn(false);
            when(roleRepository.findByName("REPORTER")).thenReturn(Optional.of(roleReporter));
            when(encoder.encode(any())).thenReturn("encodedPassword");

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User registered successfully!"));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getRole().getName()).isEqualTo("REPORTER");
        }

        @Test
        @DisplayName("Should return 400 Bad Request when username is already taken")
        void shouldRejectDuplicateUsername() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setUsername("existinguser");
            request.setEmail("user@example.com");
            request.setPassword("password123");

            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when email is already in use")
        void shouldRejectDuplicateEmail() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setUsername("newuser");
            request.setEmail("existing@example.com");
            request.setPassword("password123");

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when payload is invalid (missing required fields)")
        void shouldRejectInvalidRegistrationPayload() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setUsername(""); // blank username
            request.setEmail("not-an-email"); // invalid email format
            request.setPassword(null); // missing password

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors").isMap());

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("User Login Endpoint Tests")
    class LoginTests {

        @Test
        @DisplayName("Should authenticate user and return JWT token and profile details")
        void shouldAuthenticateUserSuccessfully() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("reporter1");
            request.setPassword("password123");

            UserDetailsImpl userDetails = new UserDetailsImpl(
                    1L,
                    "reporter1",
                    "reporter1@test.com",
                    1L,
                    "encodedPass",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_REPORTER"))
            );

            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
            when(jwtUtils.generateJwtToken(auth)).thenReturn("mock.jwt.token");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("reporter1"))
                    .andExpect(jsonPath("$.email").value("reporter1@test.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_REPORTER"))
                    .andExpect(jsonPath("$.departmentId").value(1));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when credentials are bad")
        void shouldReturn401OnBadCredentials() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("reporter1");
            request.setPassword("wrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when login request is missing required fields")
        void shouldReturn400OnBlankLoginFields() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("");
            request.setPassword("");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}

