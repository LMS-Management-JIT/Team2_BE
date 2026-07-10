package com.lms.backend.dto.request;

import jakarta.validation.constraints.Max;
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
public class LessonProgressRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Lesson ID is required")
    private String lessonId;

    @NotNull(message = "Progress is required")
    @Min(value = 0, message = "Progress must be at least 0")
    @Max(value = 100, message = "Progress cannot exceed 100")
    private Integer progress;

    @NotNull(message = "Completed status is required")
    private Boolean completed;

    @Min(value = 0, message = "Last position cannot be negative")
    private Integer lastPosition;
}