package com.lms.backend.controller;

import com.lms.backend.dto.response.AbsenceRateReportResponse;
import com.lms.backend.dto.response.ClassProgressReportResponse;
import com.lms.backend.dto.response.NewStudentReportResponse;
import com.lms.backend.dto.response.ReportDashboardResponse;
import com.lms.backend.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReportController {

    private final IReportService reportService;

    @GetMapping("/absence-rate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public AbsenceRateReportResponse getAbsenceRateReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam String groupBy
    ) {
        return reportService.getAbsenceRateReport(from, to, groupBy);
    }

    @GetMapping("/new-students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public NewStudentReportResponse getNewStudentReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return reportService.getNewStudentReport(month);
    }

    @GetMapping("/classes/{classId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ClassProgressReportResponse getClassProgressReport(@PathVariable String classId) {
        return reportService.getClassProgressReport(classId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ReportDashboardResponse getReportDashboard(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false, defaultValue = "WEEK") String groupBy
    ) {
        return reportService.getReportDashboard(yearMonth, classId, groupBy);
    }
}