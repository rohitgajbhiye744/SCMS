package com.scms.backend.config;

import com.scms.backend.entity.Role;
import com.scms.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private com.scms.backend.repository.DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedDepartments();
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
                com.scms.backend.entity.Department dept = new com.scms.backend.entity.Department();
                dept.setName(deptName);
                departmentRepository.save(dept);
            }
        }
    }
}
