package com.lms.backend.service;

import com.lms.backend.dto.request.TeacherRequest;
import com.lms.backend.dto.response.TeacherResponse;

import java.util.Map;

public interface ITeacherService {
    long countTeachers() throws Exception;

    // CREATE
    String createTeacher(TeacherRequest request) throws Exception;

    // READ ALL (có phân trang + filter)
    Map<String, Object> getAllTeachers(int page, int size, String keyword, String classId, Boolean available);

    // READ DETAIL
    TeacherResponse getTeacherDetail(String id) throws Exception;

    // UPDATE
    String updateTeacher(String id, TeacherRequest request) throws Exception;

    // DELETE
    String deleteTeacher(String id) throws Exception;
}