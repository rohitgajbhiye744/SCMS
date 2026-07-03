package com.scms.backend.controller;

import com.scms.backend.dto.ComplaintRequest;
import com.scms.backend.dto.ComplaintResponse;
import com.scms.backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('REPORTER') or hasRole('STAFF') or hasRole('DEPARTMENT_COORDINATOR')")
    public ResponseEntity<ComplaintResponse> createComplaint(
            @ModelAttribute ComplaintRequest request,
            Authentication authentication) {
        try {
            ComplaintResponse response = complaintService.createComplaint(request, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('DEPARTMENT_COORDINATOR') or hasRole('STAFF') or hasRole('REPORTER')")
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints(Authentication authentication) {
        return ResponseEntity.ok(complaintService.getAllComplaints(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('REPORTER') or hasRole('STAFF') or hasRole('DEPARTMENT_COORDINATOR')")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(complaintService.getComplaintById(id, authentication.getName()));
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('STAFF') or hasRole('DEPARTMENT_COORDINATOR')")
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false, defaultValue = "") String remarks,
            Authentication authentication) {
        return ResponseEntity.ok(complaintService.updateComplaintStatus(id, status, remarks, authentication.getName()));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('DEPARTMENT_COORDINATOR')")
    public ResponseEntity<ComplaintResponse> assignComplaint(
            @PathVariable Long id,
            @RequestParam Long staffId,
            Authentication authentication) {
        return ResponseEntity.ok(complaintService.assignComplaint(id, staffId, authentication.getName()));
    }
}
