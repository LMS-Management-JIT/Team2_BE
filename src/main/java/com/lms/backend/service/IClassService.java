package com.lms.backend.service;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassDetailResponse;
import com.lms.backend.dto.response.ClassResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IClassService {
    String createClass(ClassRequest request) throws ExecutionException, InterruptedException;
    List<ClassResponse> getAllClasses() throws ExecutionException, InterruptedException;

    ClassDetailResponse getClassDetail(String classId) throws ExecutionException, InterruptedException;

    String updateClass(String classId, ClassRequest request) throws ExecutionException, InterruptedException;
    String deleteClass(String classId, String deletedBy) throws ExecutionException, InterruptedException;

    long countClasses() throws Exception;

}