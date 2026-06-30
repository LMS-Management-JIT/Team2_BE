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
public class TopicEntity {

    @DocumentId
    private String id;

    @PropertyName("topic_name")
    private String topicName;

    @PropertyName("deleted")
    private boolean deleted;

    @PropertyName("create_date")
    private LocalDateTime createdAt;

    @PropertyName("update_date")
    private LocalDateTime updatedAt;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_by")
    private String updatedBy;

}