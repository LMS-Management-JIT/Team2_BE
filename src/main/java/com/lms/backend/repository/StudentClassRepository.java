package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.dto.request.StudentClassRequest;
import com.lms.backend.dto.response.StudentClassResponse;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.entity.StudentClassEntity;
import com.lms.backend.mapper.ClassMapper;
import com.lms.backend.mapper.StudentClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class StudentClassRepository {


    private final Firestore firestore;


    private final StudentClassMapper studentClassMapper;

    private final ClassMapper classMapper;


    private final ClassRepository classRepository;


    public CollectionReference getStudentClassCollection() {
        return firestore.collection("student_class");
    }

    // CREATE
    public String save(StudentClassEntity entity) throws ExecutionException, InterruptedException {

        DocumentReference docRef = getStudentClassCollection().document();

        entity.setId(docRef.getId());

        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            transaction.set(docRef, studentClassMapper.toFirestoreMap(entity));

            return docRef.getId();
        });

        return future.get();
    }

    // READ ONE
    public StudentClassEntity findById(String id) throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot = getStudentClassCollection().document(id).get().get();

        if (snapshot.exists()) {
            return studentClassMapper.fromDocument(snapshot);
        }

        return null;
    }

    // READ BY STUDENT
    public List<StudentClassEntity> findByStudentId(String studentId) {

        try {

            List<StudentClassEntity> result = new ArrayList<>();

            List<QueryDocumentSnapshot> docs = getStudentClassCollection().whereEqualTo("student_id", studentId).whereEqualTo("deleted", false).get().get().getDocuments();

            for (QueryDocumentSnapshot doc : docs) {

                StudentClassEntity entity = studentClassMapper.fromDocument(doc);

                if (entity != null) {
                    result.add(entity);
                }
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lỗi khi lấy danh sách lớp của học viên", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách lớp của học viên", e);
        }
    }

    public void syncStudentClasses(String studentId, List<StudentClassRequest> requests, String updatedBy) throws ExecutionException, InterruptedException {

        WriteBatch batch = firestore.batch();

        LocalDateTime now = LocalDateTime.now();
        Date updateDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        // Danh sách lớp hiện tại
        List<StudentClassEntity> oldClasses = findByStudentId(studentId);

        Map<String, StudentClassEntity> oldMap = new HashMap<>();

        for (StudentClassEntity entity : oldClasses) {
            oldMap.put(entity.getClassId(), entity);
        }

        Set<String> requestClassIds = new HashSet<>();

        // Insert hoặc Update
        if (requests != null) {

            for (StudentClassRequest item : requests) {

                requestClassIds.add(item.getClassId());

                StudentClassEntity old = oldMap.get(item.getClassId());

                // ===== Chưa có -> Insert =====
                if (old == null) {

                    DocumentReference doc = getStudentClassCollection().document();

                    StudentClassEntity entity = StudentClassEntity.builder().id(doc.getId()).studentId(studentId).classId(item.getClassId()).status(item.getStatus()).deleted(false).createdBy(updatedBy).updatedBy(updatedBy).createDate(now).updateDate(now).build();

                    batch.set(doc, studentClassMapper.toFirestoreMap(entity));

                    continue;
                }

                // ===== Có rồi nhưng status đổi =====
                if (!old.getStatus().equals(item.getStatus())) {

                    batch.update(getStudentClassCollection().document(old.getId()), "status", item.getStatus(), "updated_by", updatedBy, "update_date", updateDate);
                }
            }
        }

        // Soft delete lớp bị bỏ
        for (StudentClassEntity old : oldClasses) {

            if (!requestClassIds.contains(old.getClassId())) {

                batch.update(getStudentClassCollection().document(old.getId()), "deleted", true, "updated_by", updatedBy, "update_date", updateDate);
            }
        }

        batch.commit().get();
    }

    public List<StudentClassResponse> findClassesByStudentId(String studentId) {

        try {

            List<StudentClassResponse> result = new ArrayList<>();

            List<QueryDocumentSnapshot> relations = getStudentClassCollection().whereEqualTo("student_id", studentId).whereEqualTo("deleted", false).get().get().getDocuments();

            for (QueryDocumentSnapshot relation : relations) {

                String classId = relation.getString("class_id");

                if (classId == null || classId.isBlank()) {
                    continue;
                }

                ClassEntity classEntity = classRepository.findById(classId);

                if (classEntity == null) {
                    continue;
                }

                StudentClassResponse item = StudentClassResponse.builder().classId(classId).className(classEntity.getClassName()).status(relation.getString("status")).build();

                result.add(item);
            }

            return result;

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách lớp", e);
        }
    }

    // UPDATE
    public String update(StudentClassEntity entity) throws ExecutionException, InterruptedException {

        DocumentReference docRef = getStudentClassCollection().document(entity.getId());

        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            transaction.set(docRef, studentClassMapper.toFirestoreMap(entity), SetOptions.merge());

            return "Cập nhật StudentClass thành công!";
        });

        return future.get();
    }

    // DELETE
    public void softDeleteByStudentId(String studentId, String updatedBy) throws ExecutionException, InterruptedException {

        List<StudentClassEntity> list = findByStudentId(studentId);

        if (list.isEmpty()) {
            return;
        }

        WriteBatch batch = firestore.batch();

        Date now = new Date();

        for (StudentClassEntity entity : list) {

            batch.update(getStudentClassCollection().document(entity.getId()), "deleted", true, "update_date", now, "updated_by", updatedBy);
        }

        batch.commit().get();
    }

    public List<StudentClassEntity> findAllActive() {

        try {

            List<StudentClassEntity> result = new ArrayList<>();

            List<QueryDocumentSnapshot> docs = getStudentClassCollection().whereEqualTo("deleted", false).get().get().getDocuments();

            for (QueryDocumentSnapshot doc : docs) {

                StudentClassEntity entity = studentClassMapper.fromDocument(doc);

                if (entity != null) {
                    result.add(entity);
                }
            }

            return result;

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}