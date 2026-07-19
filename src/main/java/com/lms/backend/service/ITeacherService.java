package com.lms.backend.service;

import com.lms.backend.dto.request.TeacherRequest;
import com.lms.backend.dto.response.TeacherResponse;
import com.lms.backend.dto.response.TeacherScheduleResponse;
import com.lms.backend.dto.response.TeacherStatsResponse;

import java.util.List;
import java.util.Map;

public interface ITeacherService {
    long countTeachers() throws Exception;

    // CREATE
    String createTeacher(TeacherRequest request) throws Exception;

    // READ ALL
    List<TeacherResponse> getAllTeachers() throws Exception;

    // READ DETAIL
    TeacherResponse getTeacherDetail(String id) throws Exception;

    Map<String, Object> getAllTeachers(int page, int size, String keyword, String classId, Boolean available);

    // UPDATE
    String updateTeacher(String id, TeacherRequest request) throws Exception;

    // DELETE
    String deleteTeacher(String id, String deletedBy) throws Exception;

    String getTeacherIdByAccountId(String accountId) throws Exception;

    TeacherStatsResponse getTeacherStats(String teacherId) throws Exception;

    List<TeacherScheduleResponse> getTeacherSchedules(String teacherId) throws Exception;

    Map<String, Object> getClassesByTeacher(String teacherId, int page, int size);

    String deleteTeacher(String id);
}