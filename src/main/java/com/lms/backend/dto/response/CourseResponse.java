package com.lms.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseResponse {
    private String id;
    private String courseName;
    private String description;
    private String thumbnailUrl;
    private boolean deleted;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}