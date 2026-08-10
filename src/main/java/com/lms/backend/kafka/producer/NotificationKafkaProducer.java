package com.lms.backend.kafka.producer;

import com.lms.backend.kafka.event.PushNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationKafkaProducer {

    private final KafkaTemplate<String, PushNotificationEvent> kafkaTemplate;

    @Value("${app.kafka.topic.notification-push}")
    private String topic;

    public void publish(
            String eventId,
            List<String> externalUserIds,
            String title,
            String body,
            Map<String, Object> data
    ) {
        if (externalUserIds == null || externalUserIds.isEmpty()) {
            return;
        }

        String id = eventId == null
                ? UUID.randomUUID().toString()
                : eventId;

        PushNotificationEvent event = PushNotificationEvent.builder()
                .eventId(id)
                .externalUserIds(externalUserIds)
                .title(title)
                .body(body)
                .data(data)
                .build();

        kafkaTemplate.send(topic, id, event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Không thể gửi event {} vào Kafka", id, error);
                    } else {
                        log.info("Đã gửi event {} vào Kafka", id);
                    }
                });
    }
}