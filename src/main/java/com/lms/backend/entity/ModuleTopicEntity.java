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
public class ModuleTopicEntity {

    @DocumentId
    private String id;

    @PropertyName("module_id")
    private String moduleId;

    @PropertyName("topic_id")
    private String topicId;

    @PropertyName("topic_order")
    private Integer topicOrder;

    @PropertyName("deleted")
    private Boolean deleted;

    @Exclude
    private LocalDateTime createdAt;

    @Exclude
    private LocalDateTime updatedAt;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("updated_by")
    private String updatedBy;
}