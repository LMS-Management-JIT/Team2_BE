package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseRequest {

    @NotBlank(message = "Tên khóa học không được để trống")
    private String courseName;

    private String description;

    private String thumbnailUrl;

    private String requestedBy;
}