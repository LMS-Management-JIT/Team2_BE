package com.lms.backend.service;

import com.lms.backend.dto.request.TopicLessonRequest;
import com.lms.backend.dto.response.TopicLessonResponse;

import java.util.List;

public interface ITopicLessonService {

    TopicLessonResponse create(TopicLessonRequest request);

    TopicLessonResponse getById(String id);

    List<TopicLessonResponse> getAll();

    List<TopicLessonResponse> getByTopicId(String topicId);

    List<TopicLessonResponse> getByLessonId(String lessonId);

    TopicLessonResponse update(String id, TopicLessonRequest request);

    void delete(String id);
}