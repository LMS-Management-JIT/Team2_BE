package com.lms.backend.service;

import com.lms.backend.dto.request.LessonRequest;
import com.lms.backend.dto.response.LessonResponse;
import com.lms.backend.dto.response.PagedResponse;

import java.util.List;

public interface ILessonService {

    LessonResponse createLesson(LessonRequest request);

    LessonResponse getLessonById(String lessonId);

    List<LessonResponse> getAllLessons();

    LessonResponse updateLesson(String lessonId, LessonRequest request);

    void deleteLesson(String lessonId);

    PagedResponse<LessonResponse> getLessonsByTopic(String topicId, int page, int size, String name, String status);
}