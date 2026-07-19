package com.lms.backend.service.impl;

import com.lms.backend.dto.request.LessonRequest;
import com.lms.backend.dto.response.LessonResponse;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.entity.TopicEntity;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.LessonMapper;
import com.lms.backend.repository.LessonRepository;
import com.lms.backend.service.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements ILessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;

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
    public LessonResponse createLesson(LessonRequest request) {

        LessonEntity lesson = lessonMapper.toEntity(request);

        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());
        LessonEntity savedLesson = lessonRepository.save(lesson);

        return lessonMapper.toResponse(savedLesson);
    }

    @Override
    public LessonResponse getLessonById(String lessonId) {

        LessonEntity lesson = lessonRepository.findById(lessonId);

        if (lesson == null) {
            throw new NoSuchElementException("Lesson not found");
        }

        return lessonMapper.toResponse(lesson);
    }

    @Override
    public PagedResponse<LessonResponse> getAllLessons(int page , int size,String name , String status)  {
        return executeWithCatch(() -> {
            PagedResponse<LessonEntity> entityPage = lessonRepository.findAll(page, size, name, status);
            List<LessonResponse> responseList = entityPage.getItems().stream()
                    .map(lessonMapper::toResponse)
                    .collect(Collectors.toList());

            return new PagedResponse<>(responseList, entityPage.getTotalItems(), entityPage.getPage(), entityPage.getSize(), entityPage.getTotalPages());
        }, "Lỗi khi lấy danh sách Topic theo trang");
    }

    @Override
    public LessonResponse updateLesson(String lessonId, LessonRequest request) {

        LessonEntity lesson = lessonRepository.findById(lessonId);

        if (lesson == null) {
            throw new NoSuchElementException("Lesson not found");
        }

        lessonMapper.updateEntity(lesson, request);
        lesson.setUpdatedAt(LocalDateTime.now());

        LessonEntity updatedLesson = lessonRepository.save(lesson);

        return lessonMapper.toResponse(updatedLesson);
    }

    @Override
    public void deleteLesson(String lessonId) {

        LessonEntity lesson = lessonRepository.findById(lessonId);

        if (lesson == null) {
            throw new NoSuchElementException("Lesson not found");
        }

        lessonRepository.deleteById(lessonId);
    }

    @Override
    public PagedResponse<LessonResponse> getLessonsByTopic(String topicId, int page, int size, String name, String status) {
        return null;
    }
}