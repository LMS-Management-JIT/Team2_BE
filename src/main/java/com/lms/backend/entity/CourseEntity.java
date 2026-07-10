package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity {

    @DocumentId
    private String id;

    @PropertyName("course_code")
    private String courseCode;

    @PropertyName("course_name")
    private String courseName;

    @PropertyName("description")
    private String description;

    @PropertyName("status")
    private String status;

    @PropertyName("deleted")
    private boolean deleted;

    @PropertyName("created_at")
    private LocalDateTime createdAt;

    @PropertyName("updated_at")
    private LocalDateTime updatedAt;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("updated_by")
    private String updatedBy;
}