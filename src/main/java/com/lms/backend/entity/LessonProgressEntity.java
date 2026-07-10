package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressEntity {

    @DocumentId
    private String id;

    @PropertyName("student_id")
    private String studentId;

    @PropertyName("lesson_id")
    private String lessonId;

    @PropertyName("progress")
    private Integer progress;

    @PropertyName("completed")
    private Boolean completed;

    @PropertyName("completed_at")
    private LocalDateTime completedAt;

    @PropertyName("last_position")
    private Integer lastPosition;

    @Exclude
    private LocalDateTime createdAt;

    @Exclude
    private LocalDateTime updatedAt;

    @PropertyName("deleted")
    private Boolean deleted;
}