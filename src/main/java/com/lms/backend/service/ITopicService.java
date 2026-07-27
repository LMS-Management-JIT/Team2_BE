package com.lms.backend.service;

import com.lms.backend.dto.request.TopicRequest;
import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.dto.response.TopicResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ITopicService {

    List<TopicWithLessonsResponse> getTopicsByModule(String moduleId)
            throws ExecutionException, InterruptedException;

    List<LessonSummaryResponse> getLessonsByTopic(String topicId)
            throws ExecutionException, InterruptedException;

    TopicWithLessonsResponse getTopicDetail(String topicId)
            throws ExecutionException, InterruptedException;

    PagedResponse<TopicResponse> getAllTopics(int page, int size, String name, String status)
            throws ExecutionException, InterruptedException;

    public boolean existsById(String topicId)
            throws ExecutionException, InterruptedException;
    TopicResponse create(TopicRequest request);
    TopicResponse getById(String id) throws ExecutionException, InterruptedException;
    TopicResponse update(String id, TopicRequest request) throws ExecutionException, InterruptedException;
    void delete(String id, String deletedBy) throws ExecutionException, InterruptedException;
}