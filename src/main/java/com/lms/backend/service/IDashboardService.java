package com.lms.backend.service;

import com.lms.backend.dto.response.DashboardCourseProgressResponse;
import com.lms.backend.dto.response.DashboardCourseResponse;
import com.lms.backend.dto.response.DashboardResponse;
import com.lms.backend.dto.response.DashboardTeacherResponse;

import java.util.List;

public interface IDashboardService {
    DashboardResponse getDashboardData();

    List<DashboardCourseResponse> getStudentCourses(String studentId);

    List<DashboardCourseProgressResponse> getStudentCourseProgress(String studentId);

    List<DashboardTeacherResponse> getStudentTeachers(String studentId);

    List<DashboardTeacherResponse> getAllTeachers();
}