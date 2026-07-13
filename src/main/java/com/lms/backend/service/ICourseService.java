package com.lms.backend.service;

import java.util.List;
import java.util.Map;

public interface ICourseService {
    List<Map<String, Object>> findAllCourses();
    Map<String, Object> getCourseById(String courseId);
}