package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseModuleResponse {
    private String id;
    private String courseId;
    private String moduleId;
    private Boolean deleted;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

}