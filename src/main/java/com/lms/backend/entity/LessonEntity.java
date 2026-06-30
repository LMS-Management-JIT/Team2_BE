package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonEntity {

    @DocumentId
    private String id;

    @PropertyName("lesson_name")
    private String lessonName;

    private String type;

    @PropertyName("video_url")
    private String videoUrl;

    private String content;

    @PropertyName("create_date")
    private LocalDateTime createDate;

    @PropertyName("update_date")
    private LocalDateTime updateDate;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_by")
    private String updateBy;

    // Vẫn giữ lại 2 trường này theo đúng thiết kế DB của bạn
    @PropertyName("created_at")
    private LocalDateTime createdAt;

    @PropertyName("updated_at")
    private LocalDateTime updatedAt;
}