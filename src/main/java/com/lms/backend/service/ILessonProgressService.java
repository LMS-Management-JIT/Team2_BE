package com.lms.backend.service;

import com.lms.backend.dto.request.LessonProgressRequest;
import com.lms.backend.dto.response.LessonProgressResponse;

import java.util.List;

public interface ILessonProgressService {

    LessonProgressResponse createLessonProgress(LessonProgressRequest request);
    LessonProgressResponse getLessonProgressById(String progressId);
    List<LessonProgressResponse> getAllLessonProgress();
    List<LessonProgressResponse> getByStudentId(String studentId);
    LessonProgressResponse updateLessonProgress(
            String progressId,
            LessonProgressRequest request
    );

    void deleteLessonProgress(String progressId);
}