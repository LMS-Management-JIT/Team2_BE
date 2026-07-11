package com.lms.backend.service;

import com.lms.backend.dto.request.StudentRequest;
import com.lms.backend.dto.response.StudentResponse;

import java.util.Map;

public interface IStudentService {
    long countStudents() throws Exception;

    // CREATE
    String createStudent(StudentRequest request) throws Exception;

    // READ ALL
    Map<String, Object> getAllStudents(int page, int size, String keyword, String classId, String status);

    // READ DETAIL
    StudentResponse getStudentDetail(String id) throws Exception;

    // UPDATE
    String updateStudent(String id, StudentRequest request) throws Exception;

    // DELETE
    String deleteStudent(String id);


}