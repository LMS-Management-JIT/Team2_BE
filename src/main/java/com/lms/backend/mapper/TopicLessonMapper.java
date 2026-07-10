package com.lms.backend.mapper;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.TopicLessonRequest;
import com.lms.backend.dto.response.TopicLessonResponse;
import com.lms.backend.entity.TopicLessonEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TopicLessonMapper {

    public TopicLessonEntity toEntity(TopicLessonRequest request) {

        if (request == null) {
            return null;
        }

        return TopicLessonEntity.builder()
                .topicId(request.getTopicId())
                .lessonId(request.getLessonId())
                .lessonOrder(request.getLessonOrder())
                .build();
    }

    public TopicLessonResponse toResponse(TopicLessonEntity entity) {

        if (entity == null) {
            return null;
        }

        return TopicLessonResponse.builder()
                .id(entity.getId())
                .topicId(entity.getTopicId())
                .lessonId(entity.getLessonId())
                .lessonOrder(entity.getLessonOrder())
                .deleted(entity.getDeleted())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntity(
            TopicLessonEntity entity,
            TopicLessonRequest request) {

        entity.setTopicId(request.getTopicId());
        entity.setLessonId(request.getLessonId());
        entity.setLessonOrder(request.getLessonOrder());
    }

    // ================= FIRESTORE =================

    public Map<String, Object> toFirestoreMap(TopicLessonEntity entity) {

        Map<String, Object> map = new HashMap<>();

        map.put("topic_id", entity.getTopicId());
        map.put("lesson_id", entity.getLessonId());
        map.put("lesson_order", entity.getLessonOrder());

        map.put("deleted", entity.getDeleted());

        map.put("created_by", entity.getCreatedBy());
        map.put("updated_by", entity.getUpdatedBy());

        map.put("created_at", toTimestamp(entity.getCreatedAt()));
        map.put("updated_at", toTimestamp(entity.getUpdatedAt()));

        return map;
    }

    public TopicLessonEntity fromDocument(DocumentSnapshot doc) {

        Long lessonOrder = doc.getLong("lesson_order");

        return TopicLessonEntity.builder()
                .id(doc.getId())
                .topicId(doc.getString("topic_id"))
                .lessonId(doc.getString("lesson_id"))
                .lessonOrder(lessonOrder == null ? null : lessonOrder.intValue())
                .deleted(Boolean.TRUE.equals(doc.getBoolean("deleted")))
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .createdAt(toLocalDateTime(doc.getTimestamp("created_at")))
                .updatedAt(toLocalDateTime(doc.getTimestamp("updated_at")))
                .build();
    }

    // ================= TIME =================

    public Timestamp toTimestamp(LocalDateTime localDateTime) {

        if (localDateTime == null) {
            return null;
        }

        return Timestamp.of(
                Date.from(
                        localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                )
        );
    }

    public LocalDateTime toLocalDateTime(Timestamp timestamp) {

        if (timestamp == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                timestamp.toDate().toInstant(),
                ZoneId.systemDefault()
        );
    }
}