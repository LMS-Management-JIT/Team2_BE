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

    @PropertyName("topic_code")
    private String topicCode;

    @PropertyName("topic_name")
    private String topicName;

    @PropertyName("status")
    private String status;

    @PropertyName("description")
    private String description;

    @PropertyName("deleted")
    private boolean deleted;

    @PropertyName("create_date")
    private LocalDateTime createdDate;

    @PropertyName("update_date")
    private LocalDateTime updatedDate;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_by")
    private String updatedBy;

}