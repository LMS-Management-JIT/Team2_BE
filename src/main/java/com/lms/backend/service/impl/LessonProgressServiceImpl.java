package com.lms.backend.service.impl;

import com.lms.backend.dto.request.LessonProgressRequest;
import com.lms.backend.dto.response.LessonProgressResponse;
import com.lms.backend.entity.LessonProgressEntity;
import com.lms.backend.mapper.LessonProgressMapper;
import com.lms.backend.repository.LessonProgressRepository;
import com.lms.backend.service.ILessonProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonProgressServiceImpl implements ILessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonProgressMapper lessonProgressMapper;

    @Override
    public LessonProgressResponse createLessonProgress(
            LessonProgressRequest request) {

        LessonProgressEntity entity = lessonProgressMapper.toEntity(request);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(request.getCompleted())) {
            entity.setCompletedAt(LocalDateTime.now());
        }
        LessonProgressEntity saved = lessonProgressRepository.save(entity);
        return lessonProgressMapper.toResponse(saved);
    }

    @Override
    public LessonProgressResponse getLessonProgressById(
            String progressId) {

        LessonProgressEntity entity =
                lessonProgressRepository.findById(progressId);

        if (entity == null) {
            throw new RuntimeException("Lesson progress not found");
        }

        return lessonProgressMapper.toResponse(entity);
    }

    @Override
    public List<LessonProgressResponse> getAllLessonProgress() {

        return lessonProgressRepository.findAll()
                .stream()
                .map(lessonProgressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonProgressResponse> getByStudentId(
            String studentId) {

        return lessonProgressRepository.findByStudentId(studentId)
                .stream()
                .map(lessonProgressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LessonProgressResponse updateLessonProgress(
            String progressId,
            LessonProgressRequest request) {

        LessonProgressEntity entity =
                lessonProgressRepository.findById(progressId);

        if (entity == null) {
            throw new RuntimeException("Lesson progress not found");
        }
        lessonProgressMapper.updateEntityFromRequest(request, entity);
        if (Boolean.TRUE.equals(request.getCompleted())
                && entity.getCompletedAt() == null) {
            entity.setCompletedAt(LocalDateTime.now());
        }
        LessonProgressEntity updated =
                lessonProgressRepository.save(entity);
        return lessonProgressMapper.toResponse(updated);
    }

    @Override
    public void deleteLessonProgress(String progressId) {

        lessonProgressRepository.deleteById(progressId);
    }
}