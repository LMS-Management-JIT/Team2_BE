package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.entity.TeacherEntity;
import com.lms.backend.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class TeacherRepository {

    private final Firestore firestore;

    public CollectionReference getTeacherCollection() {
        return firestore.collection("teachers");
    }

    public long countActive() throws Exception {
        return getTeacherCollection()
                .whereEqualTo("deleted", false)
                .get().get()
                .size();
    }

    private final TeacherMapper teacherMapper;

    // CREATE
    public String save(TeacherEntity entity)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef = getTeacherCollection().document();
        entity.setId(docRef.getId());

        ApiFuture<String> transactionResult =
                firestore.runTransaction(transaction -> {

                    transaction.set(
                            docRef,
                            teacherMapper.teacherToFirestoreMap(entity)
                    );

                    return docRef.getId();
                });

        return transactionResult.get();
    }

    // READ ONE
    public TeacherEntity findById(String teacherId)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot = getTeacherCollection()
                .document(teacherId)
                .get()
                .get();

        if (snapshot.exists()) {
            return teacherMapper.fromDocument(snapshot);
        }

        return null;
    }

    // READ ALL
    public List<TeacherEntity> findAllActive()
            throws ExecutionException, InterruptedException {

        List<TeacherEntity> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = getTeacherCollection()
                .whereEqualTo("deleted", false)
                .get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {

            TeacherEntity entity = teacherMapper.fromDocument(doc);

            if (entity != null) {
                result.add(entity);
            }
        }

        return result;
    }

    // UPDATE
    public String update(TeacherEntity entity)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                getTeacherCollection().document(entity.getId());

        ApiFuture<String> transactionResult =
                firestore.runTransaction(transaction -> {

                    transaction.set(
                            docRef,
                            teacherMapper.teacherToFirestoreMap(entity),
                            SetOptions.merge()
                    );

                    return "Cập nhật giảng viên thành công!";
                });

        return transactionResult.get();
    }

    // SOFT DELETE
    public String softDelete(TeacherEntity entity)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                getTeacherCollection().document(entity.getId());

        ApiFuture<String> transactionResult =
                firestore.runTransaction(transaction -> {

                    transaction.set(
                            docRef,
                            teacherMapper.teacherToFirestoreMap(entity),
                            SetOptions.merge()
                    );

                    return "Đã xóa giảng viên thành công!";
                });

        return transactionResult.get();
    }

    // Danh sách lớp giảng dạy
    public List<String> getClassNamesByTeacherId(String teacherId)
            throws ExecutionException, InterruptedException {

        List<String> classNames = new ArrayList<>();

        List<QueryDocumentSnapshot> relations = firestore
                .collection("teacher_class")
                .whereEqualTo("teacher_id", teacherId)
                .whereEqualTo("deleted", false)
                .get()
                .get()
                .getDocuments();

        for (QueryDocumentSnapshot relation : relations) {

            String classId = relation.getString("class_id");

            if (classId == null) {
                continue;
            }

            DocumentSnapshot classDoc = firestore
                    .collection("classes")
                    .document(classId)
                    .get()
                    .get();

            if (classDoc.exists()) {
                classNames.add(classDoc.getString("class_name"));
            }
        }

        return classNames;
    }
    public Map<String, String> getClassMapByTeacherId(String teacherId) throws Exception {
        Map<String, String> classMap = new HashMap<>();

        List<QueryDocumentSnapshot> relations = firestore
                .collection("teacher_class")
                .whereEqualTo("teacher_id", teacherId)
                .whereEqualTo("deleted", false)
                .get().get().getDocuments();

        for (QueryDocumentSnapshot relation : relations) {
            String classId = relation.getString("class_id");
            if (classId != null) {
                DocumentSnapshot classDoc = firestore.collection("classes").document(classId).get().get();
                if (classDoc.exists() && Boolean.FALSE.equals(classDoc.getBoolean("deleted"))) {
                    classMap.put(classId, classDoc.getString("class_name"));
                }
            }
        }
        return classMap;
    }
    // Trong TeacherRepository.java
    public List<DocumentSnapshot> getDetailedClassDocsByTeacherId(String teacherId) throws Exception {
        List<DocumentSnapshot> classDocs = new ArrayList<>();

        // 1. Lấy quan hệ
        List<QueryDocumentSnapshot> relations = firestore.collection("teacher_class")
                .whereEqualTo("teacher_id", teacherId)
                .whereEqualTo("deleted", false)
                .get().get().getDocuments();

        // 2. Lấy chi tiết từng lớp (vẫn là DocumentSnapshot)
        for (QueryDocumentSnapshot relation : relations) {
            String classId = relation.getString("class_id");
            if (classId != null) {
                DocumentSnapshot classDoc = firestore.collection("classes").document(classId).get().get();
                if (classDoc.exists() && Boolean.FALSE.equals(classDoc.getBoolean("deleted"))) {
                    classDocs.add(classDoc);
                }
            }
        }
        return classDocs;
    }
    public int countUniqueStudentsByClassIds(Set<String> classIds) throws Exception {
        if (classIds == null || classIds.isEmpty()) return 0;

        Set<String> uniqueStudentIds = new HashSet<>();

        for (String classId : classIds) {
            List<QueryDocumentSnapshot> studentRelations = firestore
                    .collection("student_class")
                    .whereEqualTo("class_id", classId)
                    .whereEqualTo("deleted", false)
                    .get().get().getDocuments();

            for (QueryDocumentSnapshot doc : studentRelations) {
                String studentId = doc.getString("student_id");
                if (studentId != null) {
                    uniqueStudentIds.add(studentId);
                }
            }
        }
        return uniqueStudentIds.size();
    }

    // Lấy danh sách quan hệ lớp học của một giảng viên từ bảng teacher_class
    public List<QueryDocumentSnapshot> findClassesByTeacherId(String teacherId) throws Exception {
        return firestore.collection("teacher_class")
                .whereEqualTo("teacher_id", teacherId)
                .whereEqualTo("deleted", false)
                .get().get().getDocuments();
    }

    // Lấy thông tin chi tiết của một lớp học từ bảng classes
    public DocumentSnapshot getClassDoc(String classId) throws Exception {
        return firestore.collection("classes").document(classId).get().get();
    }
}