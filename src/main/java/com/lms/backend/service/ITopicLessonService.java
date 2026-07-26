package com.lms.backend.service;

import com.lms.backend.dto.request.AddLessonToTopicRequest;
import com.lms.backend.dto.request.ReorderLessonsRequest;
import com.lms.backend.dto.request.TopicLessonRequest;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.dto.response.TopicLessonResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ITopicLessonService {
    void delete(String id) throws ExecutionException, InterruptedException;
    TopicLessonResponse addLessonToTopic(AddLessonToTopicRequest request) throws ExecutionException, InterruptedException;

    void removeLessonFromTopic(String topicId, String lessonId, String updatedBy);

    List<TopicLessonResponse> getLessonsByTopic(String topicId);
    PagedResponse<TopicLessonResponse> getAllLessonByTopic(
            int page, int size, String topicId) throws ExecutionException, InterruptedException;
    void reorderLessons(ReorderLessonsRequest request) throws ExecutionException, InterruptedException;

    boolean exists(String topicId, String lessonId);

    TopicLessonResponse findByTopicIdAndLessonId(String topicId, String lessonId);
}
