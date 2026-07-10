package com.lms.backend.dto.request;

import com.lms.backend.entity.LessonType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    @NotBlank(message = "Lesson name is required")
    @Size(max = 255, message = "Lesson name must not exceed 255 characters")
    private String lessonName;

    @NotNull(message = "Lesson order is required")
    @Min(value = 1, message = "Lesson order must be greater than 0")
    private Integer lessonOrder;

    @NotNull(message = "Published status is required")
    private Boolean published;

    private String content;

    private String videoUrl;

    private String imageUrl;

    @Min(value = 0, message = "Duration must be greater than or equal to 0")
    private Integer duration;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String starterCode;

    private String language;

    @NotNull(message = "Lesson type is required")
    private LessonType type;
}