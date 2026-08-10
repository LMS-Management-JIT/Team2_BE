package com.lms.backend.kafka.consumer;

import com.lms.backend.kafka.event.PushNotificationEvent;
import com.lms.backend.service.IOneSignalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final IOneSignalService oneSignalService;

    @KafkaListener(
            topics = "${app.kafka.topic.notification-push}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(PushNotificationEvent event) {
        log.info("Nhận Kafka notification event: {}", event.getEventId());

        oneSignalService.sendToExternalUserIds(
                event.getExternalUserIds(),
                event.getTitle(),
                event.getBody(),
                event.getData()
        );
    }
}