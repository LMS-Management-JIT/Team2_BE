package com.lms.backend.controller;

import com.lms.backend.dto.request.SendNotificationRequest;
import com.lms.backend.dto.response.AccountOptionResponse;
import com.lms.backend.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NotificationController {

    private final INotificationService notificationService;

    // Admin gửi thông báo: toàn bộ / chỉ học sinh / chỉ giảng viên / tùy chọn
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> sendNotification(@RequestBody SendNotificationRequest request) {
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    // Test/hiển thị hộp thư của 1 account (dùng để check qua Postman trước khi FE làm UI)
    @GetMapping("/users/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getUserInbox(
            @PathVariable String accountId,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(notificationService.getUserInbox(accountId, type));
    }

    // Lấy danh sách account để admin chọn khi targetType = CUSTOM
// roleFilter (optional): STUDENT | TEACHER — không truyền -> lấy cả 2
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountOptionResponse>> getSelectableAccounts(
            @RequestParam(required = false) String roleFilter) {
        return ResponseEntity.ok(notificationService.getSelectableAccounts(roleFilter));
    }

    // 🟢 Lấy danh sách thông báo cho Teacher/Admin hiện tại
    @GetMapping("/teacher/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getTeacherMyNotifications(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(notificationService.getMyNotifications(type));
    }

    // 🟢 Mở rộng: Endpoint dùng chung cho tất cả các Role (Student/Teacher/Admin)
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getMyNotifications(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(notificationService.getMyNotifications(type));
    }
    // Đánh dấu 1 thông báo là đã đọc
    @PatchMapping("/teacher/{type}/{notificationDocId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markTeacherNotificationAsRead(
            @PathVariable String type,
            @PathVariable String notificationDocId) {
        notificationService.markNotificationAsRead(type, notificationDocId);
        return ResponseEntity.noContent().build();
    }

    // Đánh dấu TẤT CẢ thông báo là đã đọc
    @PatchMapping("/teacher/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllTeacherNotificationsAsRead() {
        notificationService.markAllNotificationsAsRead();
        return ResponseEntity.noContent().build();
    }
}