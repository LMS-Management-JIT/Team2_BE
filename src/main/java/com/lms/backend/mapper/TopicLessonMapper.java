package com.lms.backend.mapper;

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

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public TopicLessonEntity toEntity(TopicLessonRequest request) {
        if (request == null) return null;

        return TopicLessonEntity.builder()
                .topicId(request.getTopicId())
                .lessonId(request.getLessonId())
                .lessonOrder(request.getLessonOrder())
                .build();
    }

    public void updateEntity(TopicLessonEntity entity, TopicLessonRequest request) {
        if (entity == null || request == null) return;

        entity.setTopicId(request.getTopicId());
        entity.setLessonId(request.getLessonId());
        entity.setLessonOrder(request.getLessonOrder());
    }

    // ================= FIRESTORE MAP =================

    public Map<String, Object> toFirestoreMap(TopicLessonEntity entity) {
        Map<String, Object> map = new HashMap<>();

        map.put("topic_id", entity.getTopicId());
        map.put("lesson_id", entity.getLessonId());
        map.put("lesson_order", entity.getLessonOrder());
        map.put("created_by", entity.getCreatedBy());
        map.put("updated_by", entity.getUpdatedBy());

        // Timestamps as String (safe)
        if (entity.getCreatedAt() != null) {
            map.put("create_at", entity.getCreatedAt().toString());
        }
        if (entity.getUpdatedAt() != null) {
            map.put("update_at", entity.getUpdatedAt().toString());
        }

        return map;
    }

    // ================= FROM DOCUMENT =================

    public TopicLessonEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        Long orderLong = doc.getLong("lesson_order") != null ? doc.getLong("lesson_order") : doc.getLong("lessonOrder");
        Integer lessonOrder = orderLong != null ? orderLong.intValue() : null;

        return TopicLessonEntity.builder()
                .id(doc.getId())
                .topicId(doc.getString("topic_id") != null ? doc.getString("topic_id") : doc.getString("topicId"))
                .lessonId(doc.getString("lesson_id") != null ? doc.getString("lesson_id") : doc.getString("lessonId"))
                .lessonOrder(lessonOrder)
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .createdAt(convertToLocalDateTime(doc.getDate("create_at"), doc.getDate("createdAt")))
                .updatedAt(convertToLocalDateTime(doc.getDate("update_at"), doc.getDate("updatedAt")))
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

    // ================= RESPONSE =================

    public TopicLessonResponse toResponse(TopicLessonEntity entity) {
        if (entity == null) return null;

        return TopicLessonResponse.builder()
                .id(entity.getId())
                .topicId(entity.getTopicId())
                .lessonId(entity.getLessonId())
                .lessonOrder(entity.getLessonOrder())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TopicLessonEntity toEntityFromRequest(String topicId, String lessonId, String createdBy) {
        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);

        return TopicLessonEntity.builder()
                .topicId(topicId)
                .lessonId(lessonId)
                .lessonOrder(0) // will be auto-assigned in service
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}