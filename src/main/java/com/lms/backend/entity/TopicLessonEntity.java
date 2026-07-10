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
public class TopicLessonEntity {

    @DocumentId
    private String id;

    @PropertyName("topic_id")
    private String topicId;

    @PropertyName("lesson_id")
    private String lessonId;

    @PropertyName("lesson_order")
    private Integer lessonOrder;

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