package com.lms.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassNotificationRequest {
    private String title;
    private String category;   // Phân loại: Lịch học | Bài tập | Kiểm tra | Khác
    private String priority;   // Trạng thái/mức độ: Bình thường | Khẩn cấp
    private String content;    // Nội dung thông báo
    private String note;       // Ghi chú
    private String targetType; // "ALL" | "CUSTOM"
    private List<String> studentIds; // bắt buộc nếu targetType = CUSTOM
}