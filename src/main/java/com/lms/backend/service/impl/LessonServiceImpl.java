package com.lms.backend.service.impl;

import com.lms.backend.dto.request.LessonRequest;
import com.lms.backend.dto.response.LessonResponse;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.entity.TopicLessonEntity;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.LessonMapper;
import com.lms.backend.repository.LessonRepository;
import com.lms.backend.repository.TopicLessonRepository;
import com.lms.backend.service.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements ILessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final TopicLessonRepository topicLessonRepository;

    private <T> T executeWithCatch(java.util.concurrent.Callable<T> task, String errorMessage) {
        try {
            return task.call();
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(errorMessage + ": " + e.getMessage(), e);
        }
    }

    @Override
    public LessonResponse createLesson(LessonRequest request) {
        LessonEntity lesson = lessonMapper.toEntity(request);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        LessonEntity savedLesson = lessonRepository.save(lesson);
        return lessonMapper.toResponse(savedLesson);
    }

    @Override
    public LessonResponse getLessonById(String lessonId) {
        LessonEntity lesson = lessonRepository.findActiveById(lessonId);
        if (lesson == null) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }
        return lessonMapper.toResponse(lesson);
    }

    @Override
    public PagedResponse<LessonResponse> getAllLessons(int page, int size, String name, String status) {
        return executeWithCatch(() -> {
            PagedResponse<LessonEntity> entityPage = lessonRepository.findAll(page, size, name, status);

            List<LessonResponse> responseList = entityPage.getContent().stream()
                    .map(lessonMapper::toResponse)
                    .collect(Collectors.toList());

            // Fixed constructor using the improved PagedResponse
            return PagedResponse.<LessonResponse>builder()
                    .content(responseList)
                    .page(entityPage.getPage())
                    .size(entityPage.getSize())
                    .totalElements(entityPage.getTotalElements())
                    .totalPages(entityPage.getTotalPages())
                    .hasNext(entityPage.isHasNext())
                    .hasPrevious(entityPage.isHasPrevious())
                    .build();

            // Alternative using static factory (cleaner):
            // return PagedResponse.of(responseList, entityPage.getTotalElements(),
            //                        entityPage.getPage(), entityPage.getSize());

        }, "Lỗi khi lấy danh sách Lesson theo trang");
    }

    @Override
    public LessonResponse updateLesson(String lessonId, LessonRequest request) {
        LessonEntity lesson = lessonRepository.findActiveById(lessonId);
        if (lesson == null) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }

        lessonMapper.updateEntity(lesson, request);
        lesson.setUpdatedAt(LocalDateTime.now());

        LessonEntity updatedLesson = lessonRepository.save(lesson);
        return lessonMapper.toResponse(updatedLesson);
    }

    @Override
    public void deleteLesson(String lessonId) {
        LessonEntity lesson = lessonRepository.findActiveById(lessonId);
        if (lesson == null) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }
        lessonRepository.deleteById(lessonId);
    }

    @Override
    public List<LessonResponse> getAvailableLessons(String topicId) throws ExecutionException, InterruptedException {
        // 1. Get all lessonIds already in this topic
        List<TopicLessonEntity> mappings = topicLessonRepository.findByTopicId(topicId);
        Set<String> existingLessonIds = mappings.stream()
                .map(TopicLessonEntity::getLessonId)
                .collect(Collectors.toSet());

        // 2. Get all active lessons
        List<LessonEntity> allLessons = lessonRepository.findAll(0,10, "", "").getContent();

        // 3. Filter out already added lessons
        return allLessons.stream()
                .filter(lesson -> !existingLessonIds.contains(lesson.getId()))
                .map(lessonMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String lessonId) throws ExecutionException, InterruptedException {
        LessonEntity lesson = lessonRepository.findActiveById(lessonId);
        return lesson != null && !lesson.isDeleted();
    }
}