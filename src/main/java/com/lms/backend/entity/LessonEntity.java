package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonEntity {

    @DocumentId
    private String id;

    @PropertyName("lesson_name")
    private String lessonName;

    @PropertyName("lesson_order")
    private Integer lessonOrder;

    @PropertyName("published")
    private Boolean published;

    @PropertyName("content")
    private String content;

    @PropertyName("video_url")
    private String videoUrl;

    @PropertyName("image_url")
    private String imageUrl;

    @PropertyName("duration")
    private Integer duration;

    @PropertyName("description")
    private String description;

    @PropertyName("starter_code")
    private String starterCode;

    @PropertyName("language")
    private String language;

    @PropertyName("type")
    private LessonType type;

    @PropertyName("create_at")
    private LocalDateTime createdAt;

    @PropertyName("update_at")
    private LocalDateTime updatedAt;

    @PropertyName("deleted")
    private boolean deleted;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("updated_by")
    private String updatedBy;
}