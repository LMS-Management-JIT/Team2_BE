package com.lms.backend.service.impl;

import com.lms.backend.service.IOneSignalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OneSignalServiceImpl implements IOneSignalService {

    private final RestTemplate restTemplate;

    @Value("${onesignal.app-id}")
    private String appId;

    @Value("${onesignal.api-key}")
    private String apiKey;

    private static final String ONESIGNAL_API_URL = "https://onesignal.com/api/v1/notifications";

    public OneSignalServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendToExternalUserIds(List<String> externalUserIds, String title, String body, Map<String, Object> data) {
        if (externalUserIds == null || externalUserIds.isEmpty()) {
            System.out.println("OneSignal: Không có người nhận, bỏ qua gửi push.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Lưu ý: OneSignal dùng scheme "Basic" cho REST API Key (không phải "Bearer")
            headers.set("Authorization", "Basic " + apiKey);

            Map<String, Object> payload = new HashMap<>();
            payload.put("app_id", appId);
            payload.put("include_external_user_ids", externalUserIds);
            payload.put("channel_for_external_user_ids", "push");
            payload.put("headings", Map.of("en", title));
            payload.put("contents", Map.of("en", body));
            if (data != null && !data.isEmpty()) {
                payload.put("data", data);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(ONESIGNAL_API_URL, entity, String.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Lỗi gửi OneSignal: " + e.getMessage(),
                    e
            );
        }
    }
}