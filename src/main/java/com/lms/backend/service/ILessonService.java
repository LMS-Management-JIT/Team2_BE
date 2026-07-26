package com.lms.backend.service;

import com.lms.backend.dto.request.LessonRequest;
import com.lms.backend.dto.response.LessonResponse;
import com.lms.backend.dto.response.PagedResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ILessonService {

    LessonResponse createLesson(LessonRequest request);

    LessonResponse getLessonById(String lessonId);

    PagedResponse<LessonResponse> getAllLessons(int page, int size, String name, String status) throws ExecutionException, InterruptedException;

    LessonResponse updateLesson(String lessonId, LessonRequest request);

    void deleteLesson(String lessonId);

    public boolean existsById(String lessonId)
            throws ExecutionException, InterruptedException;

    List<LessonResponse> getAvailableLessons(String topicId) throws ExecutionException, InterruptedException;
}