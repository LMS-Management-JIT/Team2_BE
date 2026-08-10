package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.NotificationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private final Firestore firestore;

    // Danh sách loại thông báo hợp lệ -> tên subcollection tương ứng
    private static final Map<String, String> TYPE_TO_COLLECTION = Map.of(
            "ATTENDANCE", "attendance",
            "SYSTEM", "system",
            "CLASS", "class"
    );

    public CollectionReference getNotificationCollection() {
        return firestore.collection("notifications");
    }

    private String resolveCollectionName(String type) {
        String collectionName = TYPE_TO_COLLECTION.get(type == null ? "" : type.toUpperCase());
        if (collectionName == null) {
            throw new IllegalArgumentException("Loại thông báo không hợp lệ: " + type);
        }
        return collectionName;
    }

    // Lưu log thông báo ở cấp gốc (phục vụ tra cứu, thống kê sau này)
    public String saveNotificationLog(NotificationEntity entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getNotificationCollection().document();
        entity.setId(docRef.getId());
        entity.setCreateDate(LocalDateTime.now().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("title", entity.getTitle());
        data.put("body", entity.getBody());
        data.put("note", entity.getNote());
        data.put("type", entity.getType());
        data.put("target_type", entity.getTargetType());
        data.put("target_ids", entity.getTargetIds());
        data.put("sender_id", entity.getSenderId());
        data.put("create_date", entity.getCreateDate());

        docRef.set(data).get();
        return docRef.getId();
    }

    // Ghi vào đúng subcollection theo loại (attendance | system) thay vì gộp chung "items"
    public void pushToUserInbox(String accountId, String title, String body, String note, String type,
                                String classId, String sessionId, String notificationId) {
        try {
            String collectionName = resolveCollectionName(type);

            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("body", body);
            data.put("note", note);
            data.put("type", type);
            data.put("class_id", classId);
            data.put("session_id", sessionId);
            data.put("notification_id", notificationId);
            data.put("is_read", false);
            data.put("created_at", LocalDateTime.now().toString());

            firestore.collection("user_notifications")
                    .document(accountId)
                    .collection(collectionName)
                    .add(data);
        } catch (Exception e) {
            System.err.println("Lỗi lưu inbox cho account " + accountId + ": " + e.getMessage());
        }
    }

    // Lấy thông báo của 1 account theo loại cụ thể
    public List<Map<String, Object>> findUserInboxByType(String accountId, String type) throws ExecutionException, InterruptedException {
        String collectionName = resolveCollectionName(type);
        return queryInboxCollection(accountId, collectionName);
    }

    // Lấy TẤT CẢ thông báo của 1 account (gộp cả 2 loại, sort theo thời gian mới nhất)
    public List<Map<String, Object>> findAllUserInbox(String accountId) throws ExecutionException, InterruptedException {
        List<Map<String, Object>> result = new ArrayList<>();

        for (String collectionName : TYPE_TO_COLLECTION.values()) {
            result.addAll(queryInboxCollection(accountId, collectionName));
        }

        result.sort((a, b) -> {
            String createdAtA = (String) a.getOrDefault("created_at", "");
            String createdAtB = (String) b.getOrDefault("created_at", "");
            return createdAtB.compareTo(createdAtA); // giảm dần - mới nhất trước
        });

        return result;
    }

    private List<Map<String, Object>> queryInboxCollection(String accountId, String collectionName) throws ExecutionException, InterruptedException {
        List<Map<String, Object>> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = firestore.collection("user_notifications")
                .document(accountId)
                .collection(collectionName)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            Map<String, Object> item = new HashMap<>(doc.getData());
            item.put("id", doc.getId());
            result.add(item);
        }

        return result;
    }

    // 1. Cập nhật is_read = true cho 1 thông báo cụ thể
    public void markAsRead(String accountId, String type, String notificationDocId) {
        try {
            String collectionName = resolveCollectionName(type);

            DocumentReference docRef = firestore.collection("user_notifications")
                    .document(accountId)
                    .collection(collectionName)
                    .document(notificationDocId);

            docRef.update("is_read", true).get();
        } catch (Exception e) {
            System.err.println("Lỗi đánh dấu đã đọc cho docId " + notificationDocId + ": " + e.getMessage());
        }
    }

    // 2. Cập nhật is_read = true cho TẤT CẢ thông báo của user
    public void markAllAsRead(String accountId) {
        try {
            WriteBatch batch = firestore.batch();

            for (String collectionName : TYPE_TO_COLLECTION.values()) {
                CollectionReference colRef = firestore.collection("user_notifications")
                        .document(accountId)
                        .collection(collectionName);

                // Tìm tất cả doc có is_read == false
                ApiFuture<QuerySnapshot> future = colRef.whereEqualTo("is_read", false).get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();

                for (QueryDocumentSnapshot doc : docs) {
                    batch.update(doc.getReference(), "is_read", true);
                }
            }

            // Thực thi batch update trên Firestore
            batch.commit().get();
        } catch (Exception e) {
            System.err.println("Lỗi đánh dấu tất cả đã đọc cho account " + accountId + ": " + e.getMessage());
        }
    }
}