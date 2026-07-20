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
public class AbsenceRateReportResponse {
    private String groupBy;                 // "WEEK" | "MONTH" | "YEAR"
    private List<Point> currentPeriod;
    private List<Point> previousPeriod;
    private double averageCurrentPeriod;
    private double averagePreviousPeriod;
    private double improvementPercent;
    private String peakAbsenceLabel;
    private int totalAbsences;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        private String periodLabel;      // "Tuần 1" / "Tháng 1" / "Năm 2025"
        private String dateRangeLabel;   // "1/12-7/12" / "1/2026" / "2026"
        private double absenceRate;
        private int totalSessions;       // số buổi học có trong bucket này
    }
}