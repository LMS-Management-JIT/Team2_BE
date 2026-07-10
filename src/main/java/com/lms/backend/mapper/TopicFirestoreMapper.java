package com.lms.backend.mapper;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.entity.TopicEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TopicFirestoreMapper {

    public Map<String, Object> toMap(TopicEntity topic) {

        Map<String, Object> map = new HashMap<>();

        map.put("topic_name", topic.getTopicName());
        map.put("deleted", topic.isDeleted());

        map.put("created_by", topic.getCreatedBy());
        map.put("update_by", topic.getUpdatedBy());

        map.put("created_at", toTimestamp(topic.getCreatedAt()));
        map.put("updated_at", toTimestamp(topic.getUpdatedAt()));

        return map;
    }

    public TopicEntity fromDocument(DocumentSnapshot doc) {

        return TopicEntity.builder()
                .id(doc.getId())
                .topicName(doc.getString("topic_name"))
                .deleted(Boolean.TRUE.equals(doc.getBoolean("deleted")))
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("update_by"))
                .createdAt(toLocalDateTime(doc.getTimestamp("created_at")))
                .updatedAt(toLocalDateTime(doc.getTimestamp("updated_at")))
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