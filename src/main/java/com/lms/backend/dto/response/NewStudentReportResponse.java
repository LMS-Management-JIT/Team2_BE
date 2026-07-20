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
public class NewStudentReportResponse {
    private List<WeekCount> weeks;
    private int total;
    private double growthComparedLastYearPercent;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekCount {
        private String weekLabel;
        private int count;
    }
}