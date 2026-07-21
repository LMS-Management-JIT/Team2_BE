package com.lms.backend.service.impl;

import com.google.cloud.firestore.Firestore;
import com.lms.backend.dto.request.AddLessonToTopicRequest;
import com.lms.backend.dto.request.ReorderLessonsRequest;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.dto.response.TopicLessonResponse;
import com.lms.backend.entity.TopicEntity;
import com.lms.backend.entity.TopicLessonEntity;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.TopicLessonMapper;
import com.lms.backend.repository.TopicLessonRepository;
import com.lms.backend.repository.TopicRepository;
import com.lms.backend.service.ILessonService;
import com.lms.backend.service.ITopicLessonService;
import com.lms.backend.service.ITopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TopicLessonServiceImpl implements ITopicLessonService {

    private final TopicLessonRepository repository;
    private final TopicLessonMapper mapper;
    private final TopicRepository topicRepository;
    private final ITopicService topicService;
    private final ILessonService lessonService;
    private final Firestore firestore;
    @Override
    public void delete(String id) throws ExecutionException, InterruptedException {
        TopicLessonEntity entity = repository.findById(id);
        repository.delete(id);

        // Reorder remaining lessons in the topic
        List<TopicLessonEntity> remaining = repository.findByTopicId(entity.getTopicId());
        IntStream.range(0, remaining.size())
                .forEach(i -> {
                    TopicLessonEntity rel = remaining.get(i);
                    repository.updateLessonOrder(rel.getId(), i + 1);
                });
    }

    @Override
    public TopicLessonResponse addLessonToTopic(AddLessonToTopicRequest request) throws ExecutionException, InterruptedException {
        // Business checks
        if (!topicService.existsById(request.getTopicId()) ) {
            throw new ResourceNotFoundException("Topic not found with id: " + request.getTopicId());
        }
        if (!lessonService.existsById(request.getLessonId())) {
            throw new ResourceNotFoundException("Lesson not found with id: " + request.getLessonId());
        }
        if (repository.exists(request.getTopicId(), request.getLessonId())) {
            throw new IllegalArgumentException("Lesson already associated with this topic");
        }

        // Auto-assign order
        long count = repository.countByTopic(request.getTopicId());
        int nextOrder = (int) count + 1;

        TopicLessonEntity entity = TopicLessonEntity.builder()
                .topicId(request.getTopicId())
                .lessonId(request.getLessonId())
                .lessonOrder(nextOrder)
                .createdBy(request.getCreatedBy())
                .updatedBy(request.getCreatedBy())
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .updatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();

        TopicLessonEntity saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public void removeLessonFromTopic(String topicId, String lessonId, String updatedBy) {
        TopicLessonEntity mapping = repository.findByTopicIdAndLessonId(topicId, lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found for topic: " + topicId + " and lesson: " + lessonId));

        // Transaction for atomic delete + reorder
        firestore.runTransaction(tx -> {
            repository.delete(mapping.getId());

            // Reorder remaining
            List<TopicLessonEntity> remaining = repository.findByTopicId(topicId);
            IntStream.range(0, remaining.size())
                    .forEach(i -> {
                        TopicLessonEntity rel = remaining.get(i);
                        repository.updateLessonOrder(rel.getId(), i + 1);
                    });
            return null;
        });
    }

    @Override
    public List<TopicLessonResponse> getLessonsByTopic(String topicId) {
        List<TopicLessonEntity> entities = repository.findByTopicId(topicId);
        return entities.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<TopicLessonResponse> getAllLessonByTopic(
            int page, int size, String topicId) {

        List<TopicLessonEntity> entities = repository.findAllByTopicAndFilters(topicId, page, size);

        boolean hasNext = entities.size() > size;
        if (hasNext) {
            entities = entities.subList(0, size);
        }

        List<TopicLessonResponse> responses = entities.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        long totalElements = repository.countByTopicId(topicId);

        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        return PagedResponse.<TopicLessonResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(page > 0)
                .build();
    }

    @Override
    public void reorderLessons(ReorderLessonsRequest request) throws ExecutionException, InterruptedException {
        if (!topicService.existsById(request.getTopicId())) {
            throw new ResourceNotFoundException("Topic not found: " + request.getTopicId());
        }

        List<String> lessonIds = request.getLessonIds();
        if (lessonIds == null || lessonIds.isEmpty()) {
            return;
        }

        firestore.runTransaction(tx -> {
            for (int i = 0; i < lessonIds.size(); i++) {
                String lessonId = lessonIds.get(i);
                TopicLessonEntity entity = repository.findByTopicIdAndLessonId(request.getTopicId(), lessonId)
                        .orElseThrow(() -> new ResourceNotFoundException("Lesson not in topic: " + lessonId));
                repository.updateLessonOrder(entity.getId(), i + 1);
            }
            return null;
        });
    }

    @Override
    public boolean exists(String topicId, String lessonId) {
        return repository.exists(topicId, lessonId);
    }

    @Override
    public TopicLessonResponse findByTopicIdAndLessonId(String topicId, String lessonId) {
        TopicLessonEntity entity = repository.findByTopicIdAndLessonId(topicId, lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found"));
        return mapper.toResponse(entity);
    }
}