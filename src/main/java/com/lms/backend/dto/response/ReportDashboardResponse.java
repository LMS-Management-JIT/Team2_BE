package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDashboardResponse {
    private ClassAbsenceReportResponse classAbsences;
    private NewStudentReportResponse newStudents;
    private ClassProgressReportResponse classProgress;
}