package com.lms.backend.service;

import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface lTopicService {

    List<TopicWithLessonsResponse> getTopicsByModule(String moduleId)
            throws ExecutionException, InterruptedException;

    List<LessonSummaryResponse> getLessonsByTopic(String topicId)
            throws ExecutionException, InterruptedException;

    TopicWithLessonsResponse getTopicDetail(String topicId)
            throws ExecutionException, InterruptedException;
}