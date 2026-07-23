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
public class LessonSummaryResponse {

    private String id;

    private String lessonName;

    private LessonType type;

    private Integer lessonOrder;
    private String description;
    private Integer duration;

    private Boolean published;
    private Integer order;
    private String status;
    private LocalDateTime createdAt;
}