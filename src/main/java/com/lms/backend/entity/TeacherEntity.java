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
public class TeacherEntity {

    @DocumentId
    private String id;

    @PropertyName("profile_id")
    private String profileId;

    private String specialization;

    private String qualification;

    private String certifications;

    private Boolean available;

    private Boolean deleted;

    @Exclude
    private LocalDateTime createDate;

    @Exclude
    private LocalDateTime updateDate;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("updated_by")
    private String updatedBy;
}