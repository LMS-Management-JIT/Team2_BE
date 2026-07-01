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
public class StudentEntity {

    @DocumentId
    private String id;
    @PropertyName("profile_id")
    private String profileId;
    @PropertyName("identity_number")
    private String identityNumber;
    @Exclude
    private LocalDateTime issueDate;
    @PropertyName("issue_place")
    private String issuePlace;
    @PropertyName("education_level")
    private String educationLevel;
    @PropertyName("interview_content")
    private String interviewContent;
    @PropertyName("admission_result")
    private String admissionResult;

    private String description;

    private String status;

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