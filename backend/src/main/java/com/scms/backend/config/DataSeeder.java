package com.scms.backend.config;

import com.scms.backend.entity.Department;
import com.scms.backend.entity.Role;
import com.scms.backend.entity.User;
import com.scms.backend.repository.DepartmentRepository;
import com.scms.backend.repository.RoleRepository;
import com.scms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedDepartments();
        seedDemoUsers();
    }

    private void seedRoles() {
        List<String> roles = Arrays.asList("REPORTER", "DEPARTMENT_COORDINATOR", "STAFF", "ADMIN");
        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }

    private void seedDepartments() {
        List<String> departments = Arrays.asList("IT", "Computer Engineering", "ENTC", "Mechanical", "Civil", "AI & DS");
        for (String deptName : departments) {
            if (departmentRepository.findByName(deptName).isEmpty()) {
                Department dept = new Department();
                dept.setName(deptName);
                departmentRepository.save(dept);
            }
        }
    }

    private void seedDemoUsers() {
        Role reporterRole = roleRepository.findByName("REPORTER").orElse(null);
        Role coordinatorRole = roleRepository.findByName("DEPARTMENT_COORDINATOR").orElse(null);
        Role staffRole = roleRepository.findByName("STAFF").orElse(null);
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);

        Department itDept = departmentRepository.findByName("IT").orElse(null);

        // Demo Admin User
        if (userRepository.findByUsername("admin").isEmpty() && adminRole != null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@scms.local");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setPhone("9999999990");
            admin.setRole(adminRole);
            admin.setDepartment(itDept);
            userRepository.save(admin);
        }

        // Demo Department Coordinator User
        if (userRepository.findByUsername("coordinator").isEmpty() && coordinatorRole != null) {
            User coordinator = new User();
            coordinator.setUsername("coordinator");
            coordinator.setEmail("coordinator@scms.local");
            coordinator.setPassword(passwordEncoder.encode("password123"));
            coordinator.setPhone("9999999991");
            coordinator.setRole(coordinatorRole);
            coordinator.setDepartment(itDept);
            userRepository.save(coordinator);
        }

        // Demo Staff User
        if (userRepository.findByUsername("staff").isEmpty() && staffRole != null) {
            User staff = new User();
            staff.setUsername("staff");
            staff.setEmail("staff@scms.local");
            staff.setPassword(passwordEncoder.encode("password123"));
            staff.setPhone("9999999992");
            staff.setRole(staffRole);
            staff.setDepartment(itDept);
            userRepository.save(staff);
        }

        // Demo Reporter User
        if (userRepository.findByUsername("reporter").isEmpty() && reporterRole != null) {
            User reporter = new User();
            reporter.setUsername("reporter");
            reporter.setEmail("reporter@scms.local");
            reporter.setPassword(passwordEncoder.encode("password123"));
            reporter.setPhone("9999999993");
            reporter.setRole(reporterRole);
            reporter.setDepartment(itDept);
            userRepository.save(reporter);
        }
    }
}
