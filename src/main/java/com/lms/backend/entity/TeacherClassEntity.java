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
public class TeacherClassEntity {

    @DocumentId
    private String id;

    @PropertyName("teacher_id")
    private String teacherId;

    @PropertyName("class_id")
    private String classId;

    private Boolean deleted;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("updated_by")
    private String updatedBy;

    @Exclude
    private LocalDateTime createDate;

    @Exclude
    private LocalDateTime updateDate;
}