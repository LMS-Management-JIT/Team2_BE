package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseModuleRequest {
    @NotBlank(message = "Course ID không được để trống (VD: course001)")
    private String courseId;

    @NotBlank(message = "Module ID không được để trống (VD: 03281432-...)")
    private String moduleId;

    private Boolean deleted;
    private String createdBy;
    private String updatedBy;

}