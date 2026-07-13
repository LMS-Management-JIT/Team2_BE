package com.lms.backend.mapper;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.ModuleTopicRequest;
import com.lms.backend.dto.response.ModuleTopicResponse;
import com.lms.backend.entity.ModuleTopicEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ModuleTopicMapper {

    public ModuleTopicEntity toEntity(ModuleTopicRequest request) {
        if (request == null) {
            return null;
        }

        return ModuleTopicEntity.builder()
                .moduleId(request.getModuleId())
                .topicId(request.getTopicId())
                .topicOrder(request.getTopicOrder())
                .build();
    }

    public ModuleTopicResponse toResponse(ModuleTopicEntity entity) {
        if (entity == null) {
            return null;
        }

        return ModuleTopicResponse.builder()
                .id(entity.getId())
                .moduleId(entity.getModuleId())
                .topicId(entity.getTopicId())
                .topicOrder(entity.getTopicOrder())
                .deleted(entity.getDeleted())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntity(ModuleTopicEntity entity, ModuleTopicRequest request) {
        entity.setModuleId(request.getModuleId());
        entity.setTopicId(request.getTopicId());
        entity.setTopicOrder(request.getTopicOrder());
    }


    public Map<String, Object> toFirestoreMap(ModuleTopicEntity entity) {
        Map<String, Object> map = new HashMap<>();

        map.put("module_id", entity.getModuleId());
        map.put("topic_id", entity.getTopicId());
        map.put("topic_order", entity.getTopicOrder());

        map.put("deleted", entity.getDeleted());

        map.put("created_by", entity.getCreatedBy());
        map.put("updated_by", entity.getUpdatedBy());

        map.put("created_at", toTimestamp(entity.getCreatedAt()));
        map.put("updated_at", toTimestamp(entity.getUpdatedAt()));

        return map;
    }

    public ModuleTopicEntity fromDocument(DocumentSnapshot doc) {
        Long topicOrder = doc.getLong("topic_order");

        return ModuleTopicEntity.builder()
                .id(doc.getId())
                .moduleId(doc.getString("module_id"))
                .topicId(doc.getString("topic_id"))
                .topicOrder(topicOrder == null ? null : topicOrder.intValue())
                .deleted(Boolean.TRUE.equals(doc.getBoolean("deleted")))
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .createdAt(toLocalDateTime(doc.getTimestamp("created_at")))
                .updatedAt(toLocalDateTime(doc.getTimestamp("updated_at")))
                .build();
    }

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