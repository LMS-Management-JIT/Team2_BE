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
public class DashboardResponse {
    private long totalModules;
    private long totalClasses;
    private long totalStudents;
    private long totalTeachers;
    private List<HistoryResponse> recentHistories;
}