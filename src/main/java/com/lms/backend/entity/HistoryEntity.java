package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEntity {

    @DocumentId
    private String id;

    private String action;

    @PropertyName("actor_id")
    private String actorId;

    @PropertyName("actor_name")
    private String actorName;

    private Map<String, ChangeDetail> changes;

    @PropertyName("created_at")
    private LocalDateTime createdAt;

    @PropertyName("entity_id")
    private String entityId;
    @PropertyName("entity_name")
    private String entityName;
    @PropertyName("entity_type")
    private String entityType;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeDetail {

        @PropertyName("old")
        private String oldValue;

        @PropertyName("new")
        private String newValue;
    }
}