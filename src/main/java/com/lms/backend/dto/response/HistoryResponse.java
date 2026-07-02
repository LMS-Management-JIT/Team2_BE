package com.lms.backend.dto.response;

import com.lms.backend.entity.ActionType;
import com.lms.backend.entity.HistoryEntity;
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
public class HistoryResponse {
    private String id;
    private ActionType action;
    private String actorId;
    private String actorName;
    private Map<String, HistoryEntity.ChangeDetail> changes;
    private LocalDateTime createdAt;
    private String entityId;
    private String entityName;
    private String entityType;
}