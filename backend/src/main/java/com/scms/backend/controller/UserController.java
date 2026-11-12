package com.scms.backend.controller;

import com.scms.backend.dto.UserDto;
import com.scms.backend.entity.Department;
import com.scms.backend.entity.Role;
import com.scms.backend.entity.User;
import com.scms.backend.exception.ResourceNotFoundException;
import com.scms.backend.repository.DepartmentRepository;
import com.scms.backend.repository.RoleRepository;
import com.scms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('DEPARTMENT_COORDINATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getStaffByDepartment(@RequestParam Long departmentId) {
        List<UserDto> staff = userRepository.findByDepartmentIdAndRoleName(departmentId, "STAFF").stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(staff);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role,
            @RequestParam(required = false) Long departmentId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Role newRole = roleRepository.findByName(role.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role));

        user.setRole(newRole);

        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
            user.setDepartment(department);
        }

        userRepository.save(user);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }
}
