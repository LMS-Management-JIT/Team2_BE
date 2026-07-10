package com.lms.backend.dto.response;

import com.lms.backend.entity.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSummaryResponse {

    private String id;

    private String lessonName;

    private LessonType type;

    private Integer lessonOrder;

    private Integer duration;

    private Boolean published;
}