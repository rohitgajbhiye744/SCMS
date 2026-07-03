package com.scms.backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ComplaintRequest {
    private String title;
    private String description;
    private String category;
    private String priority;
    private Long departmentId;
    private String roomNumber;
    private String qrCodeUuid; // Optional
    private List<MultipartFile> images;
}
