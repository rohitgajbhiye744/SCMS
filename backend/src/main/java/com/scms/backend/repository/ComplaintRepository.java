package com.scms.backend.repository;

import com.scms.backend.entity.Complaint;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    
    @EntityGraph(attributePaths = {"user", "assignedStaff", "department", "images"})
    List<Complaint> findByUserId(Long userId);
    
    @EntityGraph(attributePaths = {"user", "assignedStaff", "department", "images"})
    List<Complaint> findByAssignedStaffId(Long staffId);
    
    @EntityGraph(attributePaths = {"user", "assignedStaff", "department", "images"})
    List<Complaint> findByDepartmentId(Long departmentId);

    @EntityGraph(attributePaths = {"user", "assignedStaff", "department", "images"})
    Optional<Complaint> findById(Long id);
}
