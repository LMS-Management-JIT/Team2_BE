package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressResponse {

    private String id;

    private String studentId;

    private String lessonId;

    private Integer progress;

    private Boolean completed;

    private Integer lastPosition;

    private LocalDateTime completedAt;
}