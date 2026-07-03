package com.scms.backend.dto;

import com.scms.backend.entity.Complaint;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ComplaintResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private Date dateSubmitted;
    private String userEmail;
    private String assignedStaffEmail;
    private String departmentName;
    private String roomNumber;
    private List<String> imageUrls;
    
    public static ComplaintResponse fromEntity(Complaint complaint) {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(complaint.getId());
        response.setTitle(complaint.getTitle());
        response.setDescription(complaint.getDescription());
        response.setCategory(complaint.getCategory());
        response.setPriority(complaint.getPriority());
        response.setStatus(complaint.getStatus());
        response.setDateSubmitted(complaint.getDateSubmitted());
        
        if (complaint.getUser() != null) {
            response.setUserEmail(complaint.getUser().getEmail());
        }
        
        if (complaint.getAssignedStaff() != null) {
            response.setAssignedStaffEmail(complaint.getAssignedStaff().getEmail());
        }
        
        if (complaint.getDepartment() != null) {
            response.setDepartmentName(complaint.getDepartment().getName());
        }
        
        response.setRoomNumber(complaint.getRoomNumber());
        
        if (complaint.getImages() != null) {
            response.setImageUrls(complaint.getImages().stream()
                    .map(img -> img.getImageUrl())
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
}
