package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.TopicRequest;
import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.TopicResponse;
import com.lms.backend.dto.response.TopicSummaryResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.entity.TopicEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TopicMapper {

    /**
     * TopicRequest -> TopicEntity
     */
    public TopicEntity toEntity(TopicRequest request) {

        if (request == null) {
            return null;
        }

        return TopicEntity.builder()
                .topicName(request.getTopicName())
                .build();
    }

    private Date toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public TopicEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        return TopicEntity.builder()
                .id(doc.getId())
                .topicName(doc.getString("topic_name"))
                .description(doc.getString("description"))
                .deleted(Boolean.TRUE.equals(doc.getBoolean("deleted")))
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("update_by"))
                .createdAt(convertToLocalDateTime(doc, "create_date", "createdAt"))
                .updatedAt(convertToLocalDateTime(doc, "update_date", "updatedAt"))
                .build();
    }

    /**
     * Safe date converter – supports both Timestamp and String
     */
    private LocalDateTime convertToLocalDateTime(DocumentSnapshot doc, String... fieldNames) {
        for (String field : fieldNames) {
            Object value = doc.get(field);
            if (value == null) continue;

            // Case 1: Firestore Timestamp
            if (value instanceof com.google.cloud.Timestamp) {
                return ((com.google.cloud.Timestamp) value)
                        .toDate()
                        .toInstant()
                        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .toLocalDateTime();
            }

            // Case 2: java.util.Date
            if (value instanceof Date) {
                return ((Date) value).toInstant()
                        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .toLocalDateTime();
            }

            // Case 3: String (ISO format)
            if (value instanceof String) {
                try {
                    return LocalDateTime.parse((String) value);
                } catch (Exception ignored) {
                    // ignore parse error
                }
            }
        }
        return null;
    }

    /**
     * TopicEntity -> TopicSummaryResponse
     */
    public TopicSummaryResponse toSummaryResponse(TopicEntity entity) {

        if (entity == null) {
            return null;
        }

        return TopicSummaryResponse.builder()
                .id(entity.getId())
                .topicName(entity.getTopicName())
                .build();
    }

    /**
     * TopicEntity -> TopicWithLessonsResponse
     */
    public TopicWithLessonsResponse toWithLessonsResponse(TopicEntity entity) {

        if (entity == null) {
            return null;
        }

        return TopicWithLessonsResponse.builder()
                .id(entity.getId())
                .topicName(entity.getTopicName())
                .build();
    }

    /**
     * LessonEntity -> LessonSummaryResponse
     */
    public LessonSummaryResponse toLessonSummaryResponse(LessonEntity lesson) {

        if (lesson == null) {
            return null;
        }

        return LessonSummaryResponse.builder()
                .id(lesson.getId())
                .lessonName(lesson.getLessonName())
                .type(lesson.getType())
                .duration(lesson.getDuration())
                .published(lesson.getPublished())
                .build();
    }

    /**
     * Update Topic
     */
    public void updateEntity(TopicEntity entity, TopicRequest request) {

        entity.setTopicName(request.getTopicName());
    }
    public Map<String, Object> toFirestoreMap(TopicEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("topic_code", entity.getTopicCode());
        map.put("topic_name", entity.getTopicName());
        map.put("description", entity.getDescription());
        map.put("deleted", entity.isDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("update_by", entity.getUpdatedBy());

        if (entity.getCreatedAt() != null) {
            map.put("create_date", entity.getCreatedAt().toString());
        }
        if (entity.getUpdatedAt() != null) {
            map.put("update_date", entity.getUpdatedAt().toString());
        }
        return map;
    }
    public TopicResponse toResponse(TopicEntity entity) {
        if (entity == null) return null;
        return TopicResponse.builder()
                .id(entity.getId())
                .topicName(entity.getTopicName())
                .description(entity.getDescription())
                .deleted(entity.isDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}