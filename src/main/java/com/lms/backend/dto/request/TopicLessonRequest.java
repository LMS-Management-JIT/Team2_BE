package com.lms.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicLessonRequest {

    private String id;

    @NotBlank(message = "Topic ID is required")
    private String topicId;

    @NotBlank(message = "Lesson ID is required")
    private String lessonId;

    @NotNull(message = "Lesson order is required")
    @Min(value = 1, message = "Lesson order must be greater than 0")
    private Integer lessonOrder;

}