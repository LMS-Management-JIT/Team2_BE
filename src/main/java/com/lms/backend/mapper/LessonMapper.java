package com.lms.backend.mapper;

import com.lms.backend.dto.request.LessonRequest;
import com.lms.backend.dto.response.LessonResponse;
import com.lms.backend.entity.LessonEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LessonMapper {

    /**
     * Chuyển LessonRequest -> Lesson
     */
    public LessonEntity toEntity(LessonRequest request) {

        if (request == null) {
            return null;
        }

        return LessonEntity.builder()
                .lessonName(request.getLessonName())
                .type(request.getType())
                .lessonOrder(request.getLessonOrder())
                .published(request.getPublished())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .duration(request.getDuration())
                .description(request.getDescription())
                .starterCode(request.getStarterCode())
                .language(request.getLanguage())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Chuyển Lesson -> LessonResponse
     */
    public LessonResponse toResponse(LessonEntity lesson) {

        if (lesson == null) {
            return null;
        }

        return LessonResponse.builder()
                .id(lesson.getId())
                .lessonName(lesson.getLessonName())
                .type(lesson.getType())
                .lessonOrder(lesson.getLessonOrder())
                .published(lesson.getPublished())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .duration(lesson.getDuration())
                .description(lesson.getDescription())
                .starterCode(lesson.getStarterCode())
                .language(lesson.getLanguage())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    /**
     * Cập nhật Lesson từ Request
     */
    public void updateEntity(LessonEntity lesson, LessonRequest request) {

        lesson.setLessonName(request.getLessonName());
        lesson.setType(request.getType());
        lesson.setLessonOrder(request.getLessonOrder());
        lesson.setPublished(request.getPublished());

        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setDuration(request.getDuration());

        lesson.setDescription(request.getDescription());
        lesson.setStarterCode(request.getStarterCode());
        lesson.setLanguage(request.getLanguage());

        lesson.setUpdatedAt(LocalDateTime.now());
    }

}