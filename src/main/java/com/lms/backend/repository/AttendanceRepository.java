package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.AttendanceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class AttendanceRepository {

    private final Firestore firestore;

    public CollectionReference getAttendanceCollection(String classId, String sessionId) {
        return firestore.collection("classes").document(classId)
                .collection("class_sessions").document(sessionId)
                .collection("attendances");
    }

    public List<AttendanceEntity> findBySession(String classId, String sessionId) throws ExecutionException, InterruptedException {
        List<AttendanceEntity> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = getAttendanceCollection(classId, sessionId).get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            result.add(AttendanceEntity.builder()
                    .studentId(doc.getId())
                    .status(doc.getString("status"))
                    .note(doc.getString("note"))
                    .updatedBy(doc.getString("updated_by"))
                    .updateDate(doc.getString("update_date"))
                    .build());
        }

        return result;
    }

    public void saveAll(String classId, String sessionId, List<AttendanceEntity> records, String updatedBy) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        CollectionReference collection = getAttendanceCollection(classId, sessionId);
        String now = java.time.LocalDateTime.now().toString();

        for (AttendanceEntity record : records) {
            DocumentReference docRef = collection.document(record.getStudentId());
            Map<String, Object> data = Map.of(
                    "status", record.getStatus() == null ? "" : record.getStatus(),
                    "note", record.getNote() == null ? "" : record.getNote(),
                    "updated_by", updatedBy,
                    "update_date", now
            );
            batch.set(docRef, data, SetOptions.merge());
        }

        batch.commit().get();

        // Cập nhật trạng thái buổi học sang "Đã điểm danh"
        DocumentReference sessionRef = firestore.collection("classes").document(classId)
                .collection("class_sessions").document(sessionId);
        sessionRef.update("status", "Đã điểm danh").get();
    }
}