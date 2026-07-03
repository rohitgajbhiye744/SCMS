package com.scms.backend.controller;

import com.scms.backend.dto.JwtResponse;
import com.scms.backend.dto.LoginRequest;
import com.scms.backend.dto.MessageResponse;
import com.scms.backend.dto.SignupRequest;
import com.scms.backend.entity.Role;
import com.scms.backend.entity.User;
import com.scms.backend.repository.RoleRepository;
import com.scms.backend.repository.UserRepository;
import com.scms.backend.repository.DepartmentRepository;
import com.scms.backend.entity.Department;
import com.scms.backend.security.JwtUtils;
import com.scms.backend.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority())
                .orElse("ROLE_REPORTER");

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role,
                userDetails.getDepartmentId()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setPhone(signUpRequest.getPhone());

        String strRole = signUpRequest.getRole();
        Role userRole;

        if (strRole == null) {
            userRole = roleRepository.findByName("REPORTER")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        } else {
            switch (strRole.toUpperCase()) {
                case "DEPARTMENT_COORDINATOR":
                    userRole = roleRepository.findByName("DEPARTMENT_COORDINATOR")
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    break;
                case "STAFF":
                    userRole = roleRepository.findByName("STAFF")
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    break;
                case "REPORTER":
                default:
                    userRole = roleRepository.findByName("REPORTER")
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            }
        }

        user.setRole(userRole);
        
        if (signUpRequest.getDepartmentId() != null) {
            Department department = departmentRepository.findById(signUpRequest.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Error: Department is not found."));
            user.setDepartment(department);
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
