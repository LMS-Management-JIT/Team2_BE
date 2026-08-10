package com.lms.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntity {
    private String id;
    private String title;
    private String body;
    private String note;
    private String type;        // "SYSTEM" | "ATTENDANCE"
    private String targetType;  // "ALL" | "STUDENTS" | "TEACHERS" | "CUSTOM"
    private List<String> targetIds; // chỉ dùng khi targetType = CUSTOM (danh sách accountId)
    private String senderId;
    private String createDate;  // ISO string
}