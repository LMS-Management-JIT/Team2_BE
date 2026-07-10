package com.lms.backend.service.impl;

import com.lms.backend.repository.CourseRepository;
import com.lms.backend.service.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements ICourseService {

    private final CourseRepository courseRepository;

    @Override
    public List<Map<String, Object>> findAllCourses() {
        try {
            return courseRepository.findAllCourses();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy danh sách khóa học: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getCourseById(String courseId) {
        try {
            return courseRepository.getCourseById(courseId);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy thông tin khóa học: " + e.getMessage(), e);
        }
    }
}