package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.response.HistoryResponse;
import com.lms.backend.entity.ActionType;
import com.lms.backend.entity.HistoryEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class HistoryMapper {

    public HistoryResponse toHistoryResponse(
            HistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return HistoryResponse.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .actorId(entity.getActorId())
                .actorName(entity.getActorName())
                .changes(entity.getChanges())
                .createdAt(entity.getCreatedAt())
                .entityId(entity.getEntityId())
                .entityName(entity.getEntityName())
                .entityType(entity.getEntityType())
                .build();
    }

    public HistoryEntity fromDocument(
            DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return null;
        }
        return HistoryEntity.builder()
                .id(doc.getId())
                .action(getAction(doc))
                .actorId(doc.getString("actor_id"))
                .actorName(doc.getString("actor_name"))
                .entityId(doc.getString("entity_id"))
                .entityName(doc.getString("entity_name"))
                .entityType(doc.getString("entity_type"))
                .createdAt(
                        toLocalDateTime(
                                doc.getDate("created_at")
                        )
                )
                .changes(extractChanges(doc))
                .build();
    }

    public Map<String, Object> toFirestoreMap(
            HistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("action", entity.getAction() != null ? entity.getAction().name() : null);
        map.put("actor_id", entity.getActorId());
        map.put("actor_name", entity.getActorName());
        map.put("changes", entity.getChanges());
        map.put("created_at", toTimestamp(entity.getCreatedAt()));
        map.put("entity_id", entity.getEntityId());
        map.put("entity_name", entity.getEntityName());
        map.put("entity_type", entity.getEntityType());
        return map;
    }

    private ActionType getAction(
            DocumentSnapshot doc) {
        String action =
                doc.getString("action");
        return action != null
                ? ActionType.valueOf(action)
                : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, HistoryEntity.ChangeDetail>
    extractChanges(DocumentSnapshot doc) {
        Map<String, HistoryEntity.ChangeDetail>
                result = new HashMap<>();
        Object rawChanges =
                doc.get("changes");
        if (!(rawChanges instanceof Map<?, ?> changes)) {
            return result;
        }
        for (Map.Entry<?, ?> entry : changes.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> values)) {
                continue;
            }
            result.put(
                    entry.getKey().toString(),
                    buildChangeDetail(values)
            );
        }
        return result;
    }

    private HistoryEntity.ChangeDetail buildChangeDetail(
            Map<?, ?> values) {
        return HistoryEntity.ChangeDetail.builder()
                .oldValue(
                        values.get("old") != null
                                ? values.get("old").toString()
                                : null
                )
                .newValue(
                        values.get("new") != null
                                ? values.get("new").toString()
                                : null
                )
                .build();
    }

    private Date toTimestamp(
            LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(
                localDateTime
                        .atZone(
                                ZoneId.systemDefault()
                        )
                        .toInstant()
        );
    }

    private LocalDateTime toLocalDateTime(
            Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(
                date.toInstant(),
                ZoneId.systemDefault()
        );
    }
}