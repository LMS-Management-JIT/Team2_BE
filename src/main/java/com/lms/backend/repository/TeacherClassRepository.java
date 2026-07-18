package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.dto.request.TeacherClassRequest;
import com.lms.backend.dto.response.TeacherClassResponse;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.entity.TeacherClassEntity;
import com.lms.backend.mapper.ClassMapper;
import com.lms.backend.mapper.TeacherClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class TeacherClassRepository {

    private final Firestore firestore;
    private final TeacherClassMapper teacherClassMapper;
    private final ClassMapper classMapper;
    private final ClassRepository classRepository;

    public CollectionReference getTeacherClassCollection() {
        return firestore.collection("teacher_class");
    }

    // CREATE single
    public String save(TeacherClassEntity entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getTeacherClassCollection().document();
        entity.setId(docRef.getId());

        ApiFuture<String> future = firestore.runTransaction(transaction -> {
            transaction.set(docRef, teacherClassMapper.toFirestoreMap(entity));
            return docRef.getId();
        });

        return future.get();
    }

    public List<TeacherClassEntity> findAllActive() {
        try {
            List<TeacherClassEntity> result = new ArrayList<>();
            List<QueryDocumentSnapshot> docs = getTeacherClassCollection().whereEqualTo("deleted", false).get().get().getDocuments();

            for (QueryDocumentSnapshot doc : docs) {
                TeacherClassEntity entity = teacherClassMapper.fromDocument(doc);
                if (entity != null) {
                    result.add(entity);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy tất cả teacher_class", e);
        }
    }

    // READ BY TEACHER
    public List<TeacherClassEntity> findByTeacherId(String teacherId) {
        try {
            List<TeacherClassEntity> result = new ArrayList<>();

            List<QueryDocumentSnapshot> docs = getTeacherClassCollection().whereEqualTo("teacher_id", teacherId).whereEqualTo("deleted", false).get().get().getDocuments();

            for (QueryDocumentSnapshot doc : docs) {
                TeacherClassEntity entity = teacherClassMapper.fromDocument(doc);
                if (entity != null) {
                    result.add(entity);
                }
            }
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lỗi khi lấy danh sách lớp của giảng viên", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách lớp của giảng viên", e);
        }
    }

    /**
     * Đồng bộ danh sách lớp giảng dạy (thêm / xóa mềm)
     */
    public void syncTeacherClasses(String teacherId, List<TeacherClassRequest> requests, String updatedBy) throws ExecutionException, InterruptedException {

        WriteBatch batch = firestore.batch();
        LocalDateTime now = LocalDateTime.now();
        Date updateDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        // Lấy danh sách lớp hiện tại của giảng viên
        List<TeacherClassEntity> oldClasses = findByTeacherId(teacherId);
        Map<String, TeacherClassEntity> oldMap = new HashMap<>();
        for (TeacherClassEntity entity : oldClasses) {
            oldMap.put(entity.getClassId(), entity);
        }

        Set<String> requestClassIds = new HashSet<>();

        // Thêm mới hoặc giữ lại
        if (requests != null) {
            for (TeacherClassRequest item : requests) {
                requestClassIds.add(item.getClassId());

                if (!oldMap.containsKey(item.getClassId())) {
                    // Chưa có -> Insert mới
                    DocumentReference doc = getTeacherClassCollection().document();
                    TeacherClassEntity entity = TeacherClassEntity.builder().id(doc.getId()).teacherId(teacherId).classId(item.getClassId()).deleted(false).createdBy(updatedBy).updatedBy(updatedBy).createDate(now).updateDate(now).build();

                    batch.set(doc, teacherClassMapper.toFirestoreMap(entity));
                }
            }
        }

        // Soft delete những lớp bị gỡ ra
        for (TeacherClassEntity old : oldClasses) {
            if (!requestClassIds.contains(old.getClassId())) {
                batch.update(getTeacherClassCollection().document(old.getId()), "deleted", true, "updated_by", updatedBy, "update_date", updateDate);
            }
        }

        batch.commit().get();
    }

    public List<TeacherClassResponse> findClassesByTeacherId(String teacherId) {
        try {
            List<TeacherClassResponse> result = new ArrayList<>();

            List<QueryDocumentSnapshot> relations = getTeacherClassCollection().whereEqualTo("teacher_id", teacherId).whereEqualTo("deleted", false).get().get().getDocuments();

            for (QueryDocumentSnapshot relation : relations) {
                String classId = relation.getString("class_id");
                if (classId == null || classId.isBlank()) continue;

                ClassEntity classEntity = classRepository.findById(classId);
                if (classEntity == null) continue;

                TeacherClassResponse item = TeacherClassResponse.builder().classId(classId).className(classEntity.getClassName()).build();

                result.add(item);
            }
            return result;

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách lớp giảng viên", e);
        }
    }

    // SOFT DELETE ALL BY TEACHER
    public void softDeleteByTeacherId(String teacherId, String updatedBy) throws ExecutionException, InterruptedException {
        List<TeacherClassEntity> list = findByTeacherId(teacherId);
        if (list.isEmpty()) return;

        WriteBatch batch = firestore.batch();
        Date now = new Date();

        for (TeacherClassEntity entity : list) {
            batch.update(getTeacherClassCollection().document(entity.getId()), "deleted", true, "update_date", now, "updated_by", updatedBy);
        }

        batch.commit().get();
    }
}