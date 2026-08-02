package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
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
public class CourseModuleEntity {

    @DocumentId
    private String id;

    @PropertyName("course_id")
    private String courseId;

    @PropertyName("module_id")
    private String moduleId;

    @PropertyName("module_order")
    private Integer moduleOrder;

    private Boolean deleted;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("updated_by")
    private String updatedBy;

    // Khóa hoàn toàn Firebase bằng @Exclude
    @Exclude
    private LocalDateTime createDate;

    @Exclude
    private LocalDateTime updateDate;
}