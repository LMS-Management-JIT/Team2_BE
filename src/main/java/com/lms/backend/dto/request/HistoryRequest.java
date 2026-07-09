package com.lms.backend.dto.request;

import com.lms.backend.entity.ActionType;
import com.lms.backend.entity.HistoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryRequest {
    private ActionType action;
    private String actorId;
    private String actorName;
    private String entityType;
    private String entityId;
    private String entityName;
    private Map<String, HistoryEntity.ChangeDetail> changes;
}