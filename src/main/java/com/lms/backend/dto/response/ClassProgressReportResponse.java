package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassProgressReportResponse {
    private String classId;
    private String className;
    private int totalStudents;
    private int goodCount;           // >= 80%
    private int needsAttentionCount; // < 50%
    private int perfectCount;        // == 100%
    private List<StudentProgress> students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentProgress {
        private String studentId;
        private String fullName;
        private int progressPercent;
    }
}