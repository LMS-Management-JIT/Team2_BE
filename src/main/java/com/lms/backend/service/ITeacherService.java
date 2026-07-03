package com.lms.backend.service;

import com.lms.backend.dto.request.TeacherRequest;
import com.lms.backend.dto.response.TeacherResponse;

import java.util.List;

public interface ITeacherService {
    long countTeachers() throws Exception;

    // CREATE
    String createTeacher(TeacherRequest request) throws Exception;

    // READ ALL
    List<TeacherResponse> getAllTeachers() throws Exception;

    // READ DETAIL
    TeacherResponse getTeacherDetail(String id) throws Exception;

    // UPDATE
    String updateTeacher(String id, TeacherRequest request) throws Exception;

    // DELETE
    String deleteTeacher(String id, String deletedBy) throws Exception;
}