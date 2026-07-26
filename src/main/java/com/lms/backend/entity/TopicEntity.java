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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicEntity {

    @DocumentId
    private String id;

    @PropertyName("topic_code")
    private String topicCode;

    @PropertyName("topic_name")
    private String topicName;

    @PropertyName("description")
    private String description;

    @PropertyName("deleted")
    private boolean deleted;

    @Exclude
    private LocalDateTime createdAt;

    @Exclude
    private LocalDateTime updatedAt;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_by")
    private String updatedBy;

}