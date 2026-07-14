package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.IgnoreExtraProperties;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IgnoreExtraProperties
public class ClassEntity {

    @DocumentId
    private String id;

    @PropertyName("class_name")
    private String className;

    private String note;
    private String description;
    private String programe;
    private String status;
    private boolean deleted;

    @PropertyName("course_id")
    private String courseId;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_by")
    private String updateBy;

    @Exclude
    private LocalDateTime startDateClass;

    @Exclude
    private LocalDateTime endDateClass;

    @Exclude
    private LocalDateTime createDate;

    @Exclude
    private LocalDateTime updateDate;
}