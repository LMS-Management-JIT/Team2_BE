package com.lms.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherStatsResponse {
    private int totalClasses;
    private int totalStudents;
}