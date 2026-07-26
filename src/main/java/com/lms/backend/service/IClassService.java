package com.lms.backend.service;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassDetailResponse;
import com.lms.backend.dto.response.ClassResponse;

import java.util.List;
import java.util.Map;

public interface IClassService {
    String createClass(ClassRequest request);

    // Hàm lấy lịch trình lớp học gọi từ Impl
    List<Map<String, Object>> getClassSessions(String classId);

    Map<String, Object> getAllClasses(int page, int size, String name, String program, String status, String startDate, String endDate);
    ClassDetailResponse getClassDetail(String classId);

    List<Map<String, Object>> getStudentsOfClass(String classId);

    List<Map<String, Object>> getTeachersOfClass(String classId);

    String updateClass(String classId, ClassRequest request);

    String deleteClass(String classId);

    long countClasses();
}