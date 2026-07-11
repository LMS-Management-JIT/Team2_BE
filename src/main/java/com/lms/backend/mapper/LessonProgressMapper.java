package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.LessonProgressRequest;
import com.lms.backend.dto.response.LessonProgressResponse;
import com.lms.backend.entity.LessonProgressEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class LessonProgressMapper {

    public LessonProgressEntity toEntity(LessonProgressRequest request) {

        if (request == null) {
            return null;
        }

        return LessonProgressEntity.builder()
                .studentId(request.getStudentId())
                .lessonId(request.getLessonId())
                .progress(request.getProgress())
                .completed(request.getCompleted())
                .lastPosition(request.getLastPosition())
                .build();
    }

    public LessonProgressResponse toResponse(LessonProgressEntity entity) {

        if (entity == null) {
            return null;
        }

        return LessonProgressResponse.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .lessonId(entity.getLessonId())
                .progress(entity.getProgress())
                .completed(entity.getCompleted())
                .lastPosition(entity.getLastPosition())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    public void updateEntityFromRequest(
            LessonProgressRequest request,
            LessonProgressEntity entity
    ) {

        if (request == null || entity == null) {
            return;
        }

        entity.setStudentId(request.getStudentId());
        entity.setLessonId(request.getLessonId());
        entity.setProgress(request.getProgress());
        entity.setCompleted(request.getCompleted());
        entity.setLastPosition(request.getLastPosition());
    }

    public Map<String, Object> toFirestoreMap(LessonProgressEntity entity) {

        if (entity == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();

        map.put("student_id", entity.getStudentId());
        map.put("lesson_id", entity.getLessonId());
        map.put("progress", entity.getProgress());
        map.put("completed", entity.getCompleted());
        map.put("last_position", entity.getLastPosition());
        map.put("completed_at", toTimestamp(entity.getCompletedAt()));
        map.put("created_at", toTimestamp(entity.getCreatedAt()));
        map.put("updated_at", toTimestamp(entity.getUpdatedAt()));
        return map;
    }

    public LessonProgressEntity fromDocument(DocumentSnapshot doc) {

        if (doc == null || !doc.exists()) {
            return null;
        }

        LessonProgressEntity entity = doc.toObject(LessonProgressEntity.class);

        if (entity != null) {

            entity.setId(doc.getId());
            entity.setCompletedAt(
                    toLocalDateTime(doc.getDate("completed_at"))
            );
            entity.setCreatedAt(
                    toLocalDateTime(doc.getDate("created_at"))
            );

            entity.setUpdatedAt(
                    toLocalDateTime(doc.getDate("updated_at"))
            );
        }

        return entity;
    }

    private Date toTimestamp(LocalDateTime localDateTime) {

        if (localDateTime == null) {
            return null;
        }

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()
        );
    }

    private LocalDateTime toLocalDateTime(Date date) {

        if (date == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                date.toInstant(),
                ZoneId.systemDefault()
        );
    }
}