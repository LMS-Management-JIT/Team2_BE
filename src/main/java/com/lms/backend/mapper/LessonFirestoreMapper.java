package com.lms.backend.mapper;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.entity.LessonType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class LessonFirestoreMapper {

    public Map<String, Object> toMap(LessonEntity lesson) {

        Map<String, Object> map = new HashMap<>();

        map.put("lesson_name", lesson.getLessonName());
        map.put("published", lesson.getPublished());
        map.put("content", lesson.getContent());
        map.put("video_url", lesson.getVideoUrl());
        map.put("image_url", lesson.getImageUrl());
        map.put("duration", lesson.getDuration());
        map.put("description", lesson.getDescription());
        map.put("starter_code", lesson.getStarterCode());
        map.put("language", lesson.getLanguage());
        map.put("type", lesson.getType());

        map.put("create_at", toTimestamp(lesson.getCreatedAt()));
        map.put("update_at", toTimestamp(lesson.getUpdatedAt()));

        map.put("deleted", lesson.isDeleted());
        map.put("created_by", lesson.getCreatedBy());
        map.put("updated_by", lesson.getUpdatedBy());

        return map;
    }

    public LessonEntity fromDocument(DocumentSnapshot doc) {

        Long lessonOrder = doc.getLong("lesson_order");
        Long duration = doc.getLong("duration");

        return LessonEntity.builder()
                .id(doc.getId())
                .lessonName(doc.getString("lesson_name"))
                .published(doc.getBoolean("published"))
                .content(doc.getString("content"))
                .videoUrl(doc.getString("video_url"))
                .imageUrl(doc.getString("image_url"))
                .duration(duration == null ? null : duration.intValue())
                .description(doc.getString("description"))
                .starterCode(doc.getString("starter_code"))
                .language(doc.getString("language"))
                .type(doc.get("type", LessonType.class))
                .createdAt(toLocalDateTime(doc.getTimestamp("create_at")))
                .updatedAt(toLocalDateTime(doc.getTimestamp("update_at")))
                .deleted(Boolean.TRUE.equals(doc.getBoolean("deleted")))
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .build();
    }

    private Timestamp toTimestamp(LocalDateTime time) {

        if (time == null) {
            return null;
        }

        return Timestamp.of(
                Date.from(
                        time.atZone(ZoneId.systemDefault())
                                .toInstant()
                )
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {

        if (timestamp == null) {
            return null;
        }

        return timestamp.toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}