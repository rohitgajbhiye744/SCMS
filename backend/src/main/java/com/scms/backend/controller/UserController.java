package com.scms.backend.controller;

import com.scms.backend.dto.UserDto;
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

    @GetMapping("/staff")
    @PreAuthorize("hasRole('DEPARTMENT_COORDINATOR')")
    public ResponseEntity<List<UserDto>> getStaffByDepartment(@RequestParam Long departmentId) {
        List<UserDto> staff = userRepository.findByDepartmentIdAndRoleName(departmentId, "STAFF").stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(staff);
    }
}
