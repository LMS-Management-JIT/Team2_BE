package com.lms.backend.service.impl;

import com.lms.backend.dto.request.TopicLessonRequest;
import com.lms.backend.dto.response.TopicLessonResponse;
import com.lms.backend.entity.TopicLessonEntity;
import com.lms.backend.mapper.TopicLessonMapper;
import com.lms.backend.repository.TopicLessonRepository;
import com.lms.backend.service.ITopicLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicLessonServiceImpl implements ITopicLessonService {

    private final TopicLessonRepository topicLessonRepository;
    private final TopicLessonMapper topicLessonMapper;

    @Override
    public TopicLessonResponse create(TopicLessonRequest request) {

        TopicLessonEntity entity = topicLessonMapper.toEntity(request);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(false);

        TopicLessonEntity saved = topicLessonRepository.save(entity);

        return topicLessonMapper.toResponse(saved);
    }

    @Override
    public TopicLessonResponse getById(String id) {

        TopicLessonEntity entity = topicLessonRepository.findById(id);

        if (entity == null) {
            throw new NoSuchElementException("TopicLesson not found.");
        }

        return topicLessonMapper.toResponse(entity);
    }

    @Override
    public List<TopicLessonResponse> getAll() {

        return topicLessonRepository.findAll()
                .stream()
                .map(topicLessonMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopicLessonResponse> getByTopicId(String topicId) {

        return topicLessonRepository.findByTopicId(topicId)
                .stream()
                .map(topicLessonMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopicLessonResponse> getByLessonId(String lessonId) {

        return topicLessonRepository.findByLessonId(lessonId)
                .stream()
                .map(topicLessonMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TopicLessonResponse update(
            String id,
            TopicLessonRequest request) {

        TopicLessonEntity entity = topicLessonRepository.findById(id);

        if (entity == null) {
            throw new NoSuchElementException("TopicLesson not found.");
        }

        topicLessonMapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        TopicLessonEntity updated = topicLessonRepository.save(entity);
        return topicLessonMapper.toResponse(updated);
    }

    @Override
    public void delete(String id) {

        TopicLessonEntity entity = topicLessonRepository.findById(id);
        if (entity == null) {
            throw new NoSuchElementException("TopicLesson not found.");
        }
        topicLessonRepository.deleteById(id);
    }
}