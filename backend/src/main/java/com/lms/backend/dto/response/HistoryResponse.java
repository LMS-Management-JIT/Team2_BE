package com.lms.backend.dto.response;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryResponse {

    private String action;

    private String actorId;
    private String actorName;

    private String entityType;
    private String entityId;
    private String entityName;

    private Map<String, Object> changes;

    private String createdAt;
}