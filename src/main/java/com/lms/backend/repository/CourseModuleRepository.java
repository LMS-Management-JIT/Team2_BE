package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.CourseModuleEntity;
import com.lms.backend.mapper.CourseModuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class CourseModuleRepository {
    private static final String COLLECTION_NAME = "course_modules";
    private final Firestore firestore;
    private final CourseModuleMapper mapper;

    public String save(CourseModuleEntity entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        entity.setId(docRef.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToSave = mapper.toFirestoreMap(entity);
            transaction.set(docRef, dataToSave);
            return docRef.getId();
        });
        return transactionResult.get();
    }

    public Optional<CourseModuleEntity> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(id).get().get();
        if (!doc.exists()) return Optional.empty();
        return Optional.ofNullable(mapper.fromDocument(doc));
    }

    public List<CourseModuleEntity> findAll() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME).get().get();
        List<CourseModuleEntity> list = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            CourseModuleEntity entity = mapper.fromDocument(doc);
            if (entity != null) list.add(entity);
        }
        return list;
    }

    // Hàm riêng dùng để hỗ trợ Lấy danh sách Module theo CourseId
    public List<CourseModuleEntity> findByCourseId(String courseId) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("course_id", courseId)
                .whereEqualTo("deleted", false)
                .get()
                .get();

        List<CourseModuleEntity> list = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            CourseModuleEntity entity = mapper.fromDocument(doc);
            if (entity != null) list.add(entity);
        }
        return list;
    }

    public String update(String id, CourseModuleEntity entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToUpdate = mapper.toFirestoreMap(entity);
            transaction.set(docRef, dataToUpdate, SetOptions.merge());
            return "Cập nhật thành công CourseModule: " + id;
        });
        return transactionResult.get();
    }

    public String softDelete(String id, String deletedBy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<String> future = firestore.runTransaction(transaction -> {
            transaction.update(docRef,
                    "deleted", true,
                    "update_date", Instant.now(),
                    "updated_by", deletedBy
            );
            return "Đã xóa mềm CourseModule thành công!";
        });
        return future.get();
    }

    // ========== FIND BY COURSE ID + MODULE ID (giống bên ModuleTopicRepository) ==========
    public java.util.Optional<CourseModuleEntity> findByCourseIdAndModuleId(String courseId, String moduleId)
            throws ExecutionException, InterruptedException {
        Query query = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("course_id", courseId)
                .whereEqualTo("module_id", moduleId)
                .whereEqualTo("deleted", false)
                .limit(1);

        List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();
        if (docs.isEmpty()) return java.util.Optional.empty();

        return java.util.Optional.of(mapper.fromDocument(docs.get(0)));
    }

    // ========== COUNT BY COURSE ID ==========
    public long countByCourseId(String courseId) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("course_id", courseId)
                .whereEqualTo("deleted", false)
                .get()
                .get();
        return snapshot.size();
    }

    // ========== UPDATE MODULE ORDER ==========
    public void updateModuleOrder(String id, Integer newOrder) {
        try {
            firestore.collection(COLLECTION_NAME).document(id)
                    .update("module_order", newOrder)
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to update module order", e);
        }
    }
}