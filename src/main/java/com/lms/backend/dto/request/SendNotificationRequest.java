package com.lms.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendNotificationRequest {
    private String title;
    private String body;
    private String note;
    private String targetType; // "ALL" | "STUDENTS" | "TEACHERS" | "CUSTOM"
    private List<String> targetIds; // bắt buộc nếu targetType = CUSTOM (danh sách accountId)
}