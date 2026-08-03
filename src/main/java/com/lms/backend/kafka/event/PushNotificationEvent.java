package com.lms.backend.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationEvent {

    private String eventId;
    private List<String> externalUserIds;
    private String title;
    private String body;
    private Map<String, Object> data;
}