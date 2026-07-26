package com.lms.backend.service.impl;

import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse; // Dùng class này làm Core lồng nhau
import com.lms.backend.entity.*;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.TopicMapper;
import com.lms.backend.repository.TopicRepository;
import com.lms.backend.service.ITopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements ITopicService {

    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;

    @Override
    public List<TopicWithLessonsResponse> getTopicsByModule(String moduleId) throws ExecutionException, InterruptedException {
        return topicRepository.getTopicsByModule(moduleId);
    }

    @Override
    public List<LessonSummaryResponse> getLessonsByTopic(String topicId) throws ExecutionException, InterruptedException {
        return topicRepository.findLessonsByTopic(topicId);
    }

    @Override
    public TopicWithLessonsResponse getTopicDetail(String topicId) throws ExecutionException, InterruptedException {
        return topicRepository.getTopicDetail(topicId);
    }

    private <T> T executeWithCatch(Callable<T> task, String errorMessage) {
        try {
            return task.call();
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(errorMessage + ": " + e.getMessage(), e);
        }
    }

    @Override
    public PagedResponse<TopicWithLessonsResponse> getAllTopics(int page, int size, String name, String status) {
        return executeWithCatch(() -> {
            PagedResponse<TopicEntity> entityPage = topicRepository.findAllActive(page, size, name, status);
            List<TopicWithLessonsResponse> responseList = entityPage.getContent().stream()
                    .map(topicMapper::toWithLessonsResponse)
                    .collect(Collectors.toList());

            return new PagedResponse<>(responseList, entityPage.getTotalElements(), entityPage.getPage(), entityPage.getSize(), entityPage.getTotalPages());
        }, "Lỗi khi lấy danh sách Topic theo trang");
    }

    @Override
    public boolean existsById(String topicId) throws ExecutionException, InterruptedException {

        return topicRepository.findActiveById(topicId) != null && !topicRepository.findActiveById(topicId).isDeleted();
    }
}