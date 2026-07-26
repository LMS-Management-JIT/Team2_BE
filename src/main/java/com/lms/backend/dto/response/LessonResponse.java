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
public class LessonResponse {

    private String id;
    private String lessonName;
    private LessonType type;
    private Boolean published;
    private String content;
    private String videoUrl;
    private String imageUrl;
    private Integer duration;
    private String description;
    private String starterCode;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}