package com.lms.backend.service;

import com.lms.backend.dto.response.AbsenceRateReportResponse;
import com.lms.backend.dto.response.ClassAbsenceReportResponse;
import com.lms.backend.dto.response.ClassProgressReportResponse;
import com.lms.backend.dto.response.NewStudentReportResponse;
import com.lms.backend.dto.response.ReportDashboardResponse;

import java.time.LocalDate;

public interface IReportService {

    AbsenceRateReportResponse getAbsenceRateReport(LocalDate from, LocalDate to, String groupBy);

    NewStudentReportResponse getNewStudentReport(
            String periodType,
            String current,
            String compareMode,
            String comparePeriod
    );

    ClassProgressReportResponse getClassProgressReport(String classId);

    ClassAbsenceReportResponse getClassAbsenceReport(String classId, LocalDate from, LocalDate to);

    ReportDashboardResponse getReportDashboard(
            String yearMonth,
            String absenceClassId,
            String progressClassId
    );
}