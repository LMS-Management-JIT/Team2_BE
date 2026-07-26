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
public class ClassAbsenceReportResponse {
    private String classId;
    private String className;
    private String from;
    private String to;
    private int totalAbsences;
    private int totalStudentsAbsent;
    private List<StudentAbsence> students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAbsence {
        private String studentId;
        private String fullName;
        private int absenceCount;
        private List<AbsenceDetail> details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbsenceDetail {
        private String sessionId;
        private String sessionTitle;
        private String sessionDate;
        private String status;
        private String note;
    }
}