package com.lms.backend.service;

import com.lms.backend.dto.request.CourseRequest;
import com.lms.backend.dto.response.CourseResponse;
import com.lms.backend.dto.response.PagedResponse;

import java.util.List;
import java.util.Map;

public interface ICourseService {
    List<Map<String, Object>> findAllCourses();
    PagedResponse<CourseResponse> findAllCoursesPage(int page, int size, String name);
    Map<String, Object> getCourseById(String courseId);
    String createCourse(CourseRequest request);
    CourseResponse updateCourse(String courseId, CourseRequest request);
    String deleteCourse(String courseId, String deletedBy);
}