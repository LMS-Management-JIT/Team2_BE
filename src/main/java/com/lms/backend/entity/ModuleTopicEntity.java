package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleTopicEntity {
    @DocumentId
    private String id;

    @PropertyName("topic_id")
    private String topicId;

    @PropertyName("module_id")
    private String moduleId;

    private Integer order;

    private String status;

    private Boolean deleted;

    @Exclude
    private LocalDateTime createDate;

    @Exclude
    private LocalDateTime updateDate;

    @Exclude
    private String createdBy;

    @Exclude
    private String updateBy;
}
