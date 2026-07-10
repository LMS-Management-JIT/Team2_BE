package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCourseProgressResponse {
    private String courseId;
    private String courseName;
    private String description;
    private String thumbnailUrl;
    private Integer progress;
    private Integer totalLessons;
    private Integer completedLessons;
}
