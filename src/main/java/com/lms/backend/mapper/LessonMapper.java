package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.LessonRequest;
import com.lms.backend.dto.response.LessonResponse;
import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.entity.LessonType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class LessonMapper {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public LessonEntity toEntity(LessonRequest request) {
        if (request == null) return null;

        return LessonEntity.builder()
                .lessonName(request.getLessonName())
                .type(request.getType())
                .published(request.getPublished())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .imageUrl(request.getImageUrl())           // Added
                .duration(request.getDuration())
                .description(request.getDescription())
                .starterCode(request.getStarterCode())
                .language(request.getLanguage())
                .createdAt(LocalDateTime.now(VIETNAM_ZONE))
                .updatedAt(LocalDateTime.now(VIETNAM_ZONE))
                .build();
    }

    public LessonEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        String typeStr = doc.getString("type");
        LessonType type = typeStr != null ? LessonType.valueOf(typeStr) : null;

        Long durationLong = doc.getLong("duration");
        Integer duration = durationLong != null ? durationLong.intValue() : null;

        return LessonEntity.builder()
                .id(doc.getId())
                .lessonName(doc.getString("lesson_name"))
                .published(doc.getBoolean("published"))
                .content(doc.getString("content"))
                .videoUrl(doc.getString("video_url"))
                .imageUrl(doc.getString("image_url"))
                .duration(duration)
                .description(doc.getString("description"))
                .starterCode(doc.getString("starter_code"))
                .language(doc.getString("language"))
                .type(type)
                .createdAt(convertToLocalDateTime(doc.getDate("create_at"), doc.getDate("createdAt")))
                .updatedAt(convertToLocalDateTime(doc.getDate("update_at"), doc.getDate("updatedAt")))
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .build();
    }

    private LocalDateTime convertToLocalDateTime(Date... dates) {
        for (Date date : dates) {
            if (date != null) {
                return LocalDateTime.ofInstant(date.toInstant(), VIETNAM_ZONE);
            }
        }
        return null;
    }

    public LessonResponse toResponse(LessonEntity lesson) {
        if (lesson == null) return null;

        return LessonResponse.builder()
                .id(lesson.getId())
                .lessonName(lesson.getLessonName())
                .type(lesson.getType())
                .published(lesson.getPublished())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .imageUrl(lesson.getImageUrl())
                .duration(lesson.getDuration())
                .description(lesson.getDescription())
                .starterCode(lesson.getStarterCode())
                .language(lesson.getLanguage())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    public LessonSummaryResponse toSummaryResponse(LessonEntity lesson) {
        if (lesson == null) return null;

        String desc = lesson.getDescription();
        if (desc != null && desc.length() > 150) {
            desc = desc.substring(0, 147) + "...";
        }

        return LessonSummaryResponse.builder()
                .id(lesson.getId())
                .lessonName(lesson.getLessonName())
                .description(desc)
                .duration(lesson.getDuration())
                .status(lesson.getPublished() != null && lesson.getPublished() ? "PUBLISHED" : "DRAFT")
                .createdAt(lesson.getCreatedAt())
                .build();
    }

    public void updateEntity(LessonEntity lesson, LessonRequest request) {
        if (request == null || lesson == null) return;

        lesson.setLessonName(request.getLessonName());
        lesson.setType(request.getType());
        lesson.setPublished(request.getPublished());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setImageUrl(request.getImageUrl());
        lesson.setDuration(request.getDuration());
        lesson.setDescription(request.getDescription());
        lesson.setStarterCode(request.getStarterCode());
        lesson.setLanguage(request.getLanguage());
        lesson.setUpdatedAt(LocalDateTime.now(VIETNAM_ZONE));
    }
}