package com.lms.backend.service.impl;

import com.lms.backend.dto.request.LessonRequest;
import com.lms.backend.dto.response.LessonResponse;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.mapper.LessonMapper;
import com.lms.backend.repository.LessonRepository;
import com.lms.backend.service.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements ILessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;

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
    public List<LessonResponse> getAllLessons() {

        return lessonRepository.findAll()
                .stream()
                .map(lessonMapper::toResponse)
                .collect(Collectors.toList());
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
}