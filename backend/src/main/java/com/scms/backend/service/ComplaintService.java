package com.scms.backend.service;

import com.scms.backend.dto.ComplaintRequest;
import com.scms.backend.dto.ComplaintResponse;
import com.scms.backend.entity.*;
import com.scms.backend.exception.ResourceNotFoundException;
import com.scms.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    public static final List<String> VALID_STATUSES = Arrays.asList(
            "Submitted", "Assigned", "In Progress", "Resolved", "Closed"
    );

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
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (request.getPriority() == null || request.getPriority().trim().isEmpty()) {
            throw new IllegalArgumentException("Priority is required");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("Department is required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle().trim());
        complaint.setDescription(request.getDescription().trim());
        complaint.setCategory(request.getCategory().trim());
        complaint.setPriority(request.getPriority().trim());
        complaint.setStatus("Submitted");
        complaint.setUser(user);
        complaint.setDepartment(department);
        complaint.setRoomNumber(request.getRoomNumber());

        if (request.getQrCodeUuid() != null && !request.getQrCodeUuid().trim().isEmpty()) {
            LocationQR location = locationQRRepository.findByQrCodeUuid(request.getQrCodeUuid().trim())
                    .orElse(null);
            complaint.setLocation(location);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (MultipartFile file : request.getImages()) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadImage(file);
                    ComplaintImage image = new ComplaintImage();
                    image.setComplaint(savedComplaint);
                    image.setImageUrl(imageUrl);
                    imageRepository.save(image);
                }
            }
        }

        // Add history
        ComplaintHistory history = new ComplaintHistory();
        history.setComplaint(savedComplaint);
        history.setStatus("Submitted");
        history.setRemarks("Complaint submitted");
        history.setChangedBy(user);
        historyRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    public List<ComplaintResponse> getAllComplaints(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String role = user.getRole().getName();
        if ("ADMIN".equals(role)) {
            return complaintRepository.findAll().stream()
                    .map(ComplaintResponse::fromEntity)
                    .collect(Collectors.toList());
        } else if ("DEPARTMENT_COORDINATOR".equals(role)) {
            if (user.getDepartment() != null) {
                return complaintRepository.findByDepartmentId(user.getDepartment().getId()).stream()
                        .map(ComplaintResponse::fromEntity)
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        } else if ("STAFF".equals(role)) {
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        String role = user.getRole().getName();
        if ("REPORTER".equals(role)) {
            if (complaint.getUser() == null || !complaint.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Access denied: You can only view your own complaints");
            }
        } else if ("STAFF".equals(role)) {
            if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(user.getId())) {
                throw new AccessDeniedException("Access denied: Complaint is not assigned to you");
            }
        } else if ("DEPARTMENT_COORDINATOR".equals(role)) {
            if (complaint.getDepartment() == null || user.getDepartment() == null ||
                    !complaint.getDepartment().getId().equals(user.getDepartment().getId())) {
                throw new AccessDeniedException("Access denied: Complaint is outside your department");
            }
        }

        return ComplaintResponse.fromEntity(complaint);
    }

    @Transactional
    public ComplaintResponse updateComplaintStatus(Long id, String status, String remarks, String username) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed statuses: " + VALID_STATUSES);
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String role = user.getRole().getName();
        if ("REPORTER".equals(role)) {
            throw new AccessDeniedException("Access denied: Reporters cannot update complaint status");
        } else if ("STAFF".equals(role)) {
            if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(user.getId())) {
                throw new AccessDeniedException("Access denied: You can only update complaints assigned to you");
            }
        } else if ("DEPARTMENT_COORDINATOR".equals(role)) {
            if (complaint.getDepartment() == null || user.getDepartment() == null ||
                    !complaint.getDepartment().getId().equals(user.getDepartment().getId())) {
                throw new AccessDeniedException("Access denied: Complaint is outside your department");
            }
        }

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
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String role = user.getRole().getName();
        if ("DEPARTMENT_COORDINATOR".equals(role)) {
            if (complaint.getDepartment() == null || user.getDepartment() == null ||
                    !user.getDepartment().getId().equals(complaint.getDepartment().getId())) {
                throw new AccessDeniedException("Cannot assign complaint outside of your department");
            }
        } else if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("Only coordinators and admins can assign complaints");
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        if (!"STAFF".equals(staff.getRole().getName())) {
            throw new IllegalArgumentException("Assigned user is not a staff member");
        }

        // Verify staff belongs to the department responsible for the complaint
        if (staff.getDepartment() == null || complaint.getDepartment() == null ||
                !staff.getDepartment().getId().equals(complaint.getDepartment().getId())) {
            throw new IllegalArgumentException("Staff member does not belong to the department responsible for this complaint");
        }

        complaint.setAssignedStaff(staff);
        complaint.setStatus("Assigned");
        Complaint savedComplaint = complaintRepository.save(complaint);

        ComplaintHistory history = new ComplaintHistory();
        history.setComplaint(savedComplaint);
        history.setStatus("Assigned");
        history.setRemarks("Assigned to staff: " + staff.getUsername());
        history.setChangedBy(user);
        historyRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }
}
