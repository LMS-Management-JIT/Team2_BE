package com.lms.backend.controller;

import com.lms.backend.dto.response.DashboardCourseProgressResponse;
import com.lms.backend.dto.response.DashboardCourseResponse;
import com.lms.backend.dto.response.DashboardResponse;
import com.lms.backend.dto.response.DashboardTeacherResponse;
import com.lms.backend.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.Role;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DashboardController {

    private final IDashboardService dashboardService;


    @GetMapping
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/students/{studentId}/courses")
    public List<DashboardCourseResponse> getStudentCourses(@PathVariable String studentId) {
        return dashboardService.getStudentCourses(studentId);
    }

    @GetMapping("/students/{studentId}/course-progress")
    public List<DashboardCourseProgressResponse> getStudentCourseProgress(@PathVariable String studentId) {
        return dashboardService.getStudentCourseProgress(studentId);
    }
    @GetMapping("/teachers")
    public List<DashboardTeacherResponse> getAllTeachers() {
        return dashboardService.getAllTeachers();
    }
    @GetMapping("/students/{studentId}/teachers")
    public List<DashboardTeacherResponse> getStudentTeachers(@PathVariable String studentId) {
        return dashboardService.getStudentTeachers(studentId);
    }
}