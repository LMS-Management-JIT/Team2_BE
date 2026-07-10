package com.lms.backend.entity;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity {

    @DocumentId
    private String id;

    @PropertyName("courseName")
    private String courseName;

    @PropertyName("description")
    private String description;

    @PropertyName("thumbnailUrl")
    private String thumbnailUrl;

    @PropertyName("deleted")
    private boolean deleted;

    @PropertyName("createAt")
    private Timestamp createAt;

    @PropertyName("updatedAt")
    private Timestamp updatedAt;

    @PropertyName("createdBy")
    private String createdBy;

    @PropertyName("updatedBy")
    private String updatedBy;
}