package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.CourseEntity;
import com.lms.backend.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.lms.backend.dto.response.PagedResponse;

@Repository
@RequiredArgsConstructor
public class CourseRepository {

    private final Firestore firestore;
    private final CourseMapper courseMapper;

    public CollectionReference getCourseCollection() {
        return firestore.collection("courses");
    }

    // CREATE
    public String save(CourseEntity entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCourseCollection().document();
        entity.setId(docRef.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToSave = courseMapper.toFirestoreMap(entity);
            transaction.set(docRef, dataToSave);
            return docRef.getId();
        });
        return transactionResult.get();
    }

    // READ ALL ACTIVE with pagination and optional name filter
    public PagedResponse<CourseEntity> findAllActive(int page, int size, String name) throws ExecutionException, InterruptedException {
        List<CourseEntity> all = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = getCourseCollection().whereEqualTo("deleted", false).get();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            CourseEntity entity = courseMapper.fromDocument(doc);
            if (entity != null) all.add(entity);
        }

        // apply optional server-side filtering (name contains, case-insensitive)
        List<CourseEntity> filtered = new ArrayList<>();
        for (CourseEntity e : all) {
            boolean matches = true;
            if (name != null && !name.isBlank()) {
                String cn = e.getCourseName() == null ? "" : e.getCourseName();
                if (!cn.toLowerCase().contains(name.toLowerCase())) matches = false;
            }
            if (matches) filtered.add(e);
        }

        long total = filtered.size();
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        List<CourseEntity> pageItems = new ArrayList<>();
        if (fromIndex < filtered.size()) {
            pageItems = filtered.subList(fromIndex, toIndex);
        }

        int totalPages = (int) ((total + safeSize - 1) / safeSize);

        return new PagedResponse<>(pageItems, total, safePage, safeSize, totalPages);
    }

    // READ ALL - lấy danh sách khóa học chưa bị xóa
    public List<Map<String, Object>> findAllCourses() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> courses = new ArrayList<>();
        QuerySnapshot snapshot = getCourseCollection()
                .whereEqualTo("deleted", false)
                .get().get();

        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            convertTimestampsToString(data);
            courses.add(data);
        }
        return courses;
    }

    // READ DETAIL BY ID - trả về Entity
    public CourseEntity findById(String courseId) throws ExecutionException, InterruptedException {
        if (courseId == null || courseId.isEmpty()) return null;
        DocumentSnapshot doc = getCourseCollection().document(courseId).get().get();
        if (doc.exists()) {
            return courseMapper.fromDocument(doc);
        }
        return null;
    }

    // READ DETAIL BY ID - trả về Map (giữ cho API cũ)
    public Map<String, Object> getCourseById(String courseId) throws ExecutionException, InterruptedException {
        if (courseId == null || courseId.isEmpty()) return null;
        DocumentSnapshot doc = getCourseCollection().document(courseId).get().get();
        if (doc.exists()) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            convertTimestampsToString(data);
            return data;
        }
        return null;
    }

    // Chuyển đổi Timestamp Firestore thành chuỗi ISO
    private void convertTimestampsToString(Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof com.google.cloud.Timestamp) {
                com.google.cloud.Timestamp ts = (com.google.cloud.Timestamp) entry.getValue();
                data.put(entry.getKey(), ts.toDate().toInstant().toString());
            }
        }
    }

    // UPDATE
    public String update(String courseId, CourseEntity updatedEntity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCourseCollection().document(courseId);

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToUpdate = courseMapper.toFirestoreMap(updatedEntity);
            transaction.set(docRef, dataToUpdate, SetOptions.merge());
            return "Cập nhật thành công khóa học: " + courseId;
        });
        return transactionResult.get();
    }

    // SOFT DELETE
    public String softDelete(String courseId, String deletedBy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCourseCollection().document(courseId);

        ApiFuture<String> future = firestore.runTransaction(transaction -> {
            transaction.update(docRef,
                    "deleted", true,
                    "updatedBy", deletedBy,
                    "updatedAt", com.google.cloud.Timestamp.now()
            );
            return "Đã xóa mềm khóa học thành công!";
        });
        return future.get();
    }
}