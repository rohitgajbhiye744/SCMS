package com.scms.backend.service;

import com.scms.backend.dto.ComplaintRequest;
import com.scms.backend.dto.ComplaintResponse;
import com.scms.backend.entity.*;
import com.scms.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LocationQRRepository locationQRRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintImageRepository imageRepository;

    @Autowired
    private ComplaintHistoryRepository historyRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request, String username) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(request.getCategory());
        complaint.setPriority(request.getPriority());
        complaint.setStatus("Submitted");
        complaint.setUser(user);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElse(null);
            complaint.setDepartment(department);
        }

        if (request.getQrCodeUuid() != null && !request.getQrCodeUuid().isEmpty()) {
            LocationQR location = locationQRRepository.findByQrCodeUuid(request.getQrCodeUuid())
                    .orElse(null);
            complaint.setLocation(location);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (MultipartFile file : request.getImages()) {
                String imageUrl = cloudinaryService.uploadImage(file);
                ComplaintImage image = new ComplaintImage();
                image.setComplaint(savedComplaint);
                image.setImageUrl(imageUrl);
                imageRepository.save(image);
            }
        }

        // Add history
        ComplaintHistory history = new ComplaintHistory();
        history.setComplaint(savedComplaint);
        history.setStatus("Submitted");
        history.setRemarks("Complaint submitted");
        history.setChangedBy(user);
        historyRepository.save(history);

        return ComplaintResponse.fromEntity(complaintRepository.findById(savedComplaint.getId()).get());
    }

    public List<ComplaintResponse> getAllComplaints(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if ("DEPARTMENT_COORDINATOR".equals(user.getRole().getName())) {
            if (user.getDepartment() != null) {
                return complaintRepository.findByDepartmentId(user.getDepartment().getId()).stream()
                        .map(ComplaintResponse::fromEntity)
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        } else if ("STAFF".equals(user.getRole().getName())) {
            return complaintRepository.findByAssignedStaffId(user.getId()).stream()
                    .map(ComplaintResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        
        // REPORTER or fallback
        return complaintRepository.findByUserId(user.getId()).stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ComplaintResponse getComplaintById(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
                
        if ("REPORTER".equals(user.getRole().getName())) {
            if (!complaint.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied: You can only view your own complaints");
            }
        } else if ("STAFF".equals(user.getRole().getName())) {
            if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied: Complaint is not assigned to you");
            }
        } else if ("DEPARTMENT_COORDINATOR".equals(user.getRole().getName())) {
            if (complaint.getDepartment() == null || user.getDepartment() == null || !complaint.getDepartment().getId().equals(user.getDepartment().getId())) {
                throw new RuntimeException("Access denied: Complaint is outside your department");
            }
        }
        
        return ComplaintResponse.fromEntity(complaint);
    }
    
    @Transactional
    public ComplaintResponse updateComplaintStatus(Long id, String status, String remarks, String username) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
                
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        complaint.setStatus(status);
        Complaint savedComplaint = complaintRepository.save(complaint);
        
        ComplaintHistory history = new ComplaintHistory();
        history.setComplaint(savedComplaint);
        history.setStatus(status);
        history.setRemarks(remarks);
        history.setChangedBy(user);
        historyRepository.save(history);
        
        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional
    public ComplaintResponse assignComplaint(Long id, Long staffId, String username) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
                
        User coordinator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Coordinator not found"));
                
        if (complaint.getDepartment() == null || coordinator.getDepartment() == null || 
            !coordinator.getDepartment().getId().equals(complaint.getDepartment().getId())) {
            throw new RuntimeException("Cannot assign complaint outside of your department");
        }
        
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
                
        if (!"STAFF".equals(staff.getRole().getName())) {
            throw new RuntimeException("Assigned user is not a staff member");
        }
                
        complaint.setAssignedStaff(staff);
        complaint.setStatus("Assigned");
        Complaint savedComplaint = complaintRepository.save(complaint);
        
        ComplaintHistory history = new ComplaintHistory();
        history.setComplaint(savedComplaint);
        history.setStatus("Assigned");
        history.setRemarks("Assigned to staff: " + staff.getUsername());
        history.setChangedBy(coordinator);
        historyRepository.save(history);
        
        return ComplaintResponse.fromEntity(savedComplaint);
    }
}
