package com.lms.backend.dto.response;

import com.lms.backend.entity.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicLessonResponse {
    private String id;
    private String topicId;
    private String lessonId;
    private Integer lessonOrder;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined fields from Lesson
    private String lessonName;
    private Integer duration;
    private LessonType type;
    private Boolean published;
}