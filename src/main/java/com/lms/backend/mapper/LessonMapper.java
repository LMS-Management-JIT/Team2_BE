package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.LessonRequest;
import com.lms.backend.dto.response.LessonResponse;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.entity.LessonType;
import com.lms.backend.entity.TopicEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.threeten.bp.DateTimeUtils.toLocalDateTime;

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
    public LessonEntity fromDocument(DocumentSnapshot doc)
    {
        if (doc == null || !doc.exists()) return null;
        LessonEntity lesson = LessonEntity.builder()
                .id(doc.getId())
                .type(LessonType.valueOf(doc.getString("type")))
                .lessonName(doc.getString("lessonName"))
                .lessonOrder(Math.toIntExact(doc.getLong("lessonOrder")))
                .published(doc.getBoolean("published"))
                .content(doc.getString("content"))
                .videoUrl(doc.getString("videoUrl"))
                .duration(Math.toIntExact(doc.getLong("duration")))
                .description(doc.getString("description"))
                .starterCode(doc.getString("starterCode"))
                .language(doc.getString("language"))
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .createdAt(toLocalDateTime(doc.getDate("create_date")))
                .updatedAt(toLocalDateTime(doc.getDate("update_date")))
                .build();
        return lesson;
    }
    private Date toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
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