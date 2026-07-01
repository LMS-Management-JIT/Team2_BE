package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.ClassEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class ClassRepository {

    @Autowired
    private Firestore firestore;

    private CollectionReference getClassCollection() {
        return firestore.collection("classes");
    }

    // CREATE
    public String save(ClassEntity entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getClassCollection().document();
        entity.setId(docRef.getId());
        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            transaction.set(docRef, entity);
            return docRef.getId();
        });
        return transactionResult.get();
    }

    // READ ALL ACTIVE
    public List<ClassEntity> findAllActive() throws ExecutionException, InterruptedException {
        List<ClassEntity> result = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = getClassCollection().whereEqualTo("deleted", false).get();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            ClassEntity entity = doc.toObject(ClassEntity.class);
            entity.setId(doc.getId());
            result.add(entity);
        }
        return result;
    }

    // LẤY THÔNG TIN 1 LỚP HỌC
    public ClassEntity findById(String classId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = getClassCollection().document(classId).get().get();
        if (snapshot.exists()) {
            ClassEntity entity = snapshot.toObject(ClassEntity.class);
            entity.setId(snapshot.getId());
            return entity;
        }
        return null;
    }

    // LẤY THÔNG TIN KHÓA HỌC
    public Map<String, Object> getCourseById(String courseId) throws ExecutionException, InterruptedException {
        if (courseId == null || courseId.isEmpty()) return null;
        DocumentSnapshot doc = firestore.collection("courses").document(courseId).get().get();
        if (doc.exists()) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            return data;
        }
        return null;
    }

    // LẤY DANH SÁCH SINH VIÊN (Từ bảng trung gian student_class)
    public List<Map<String, Object>> getStudentsByClassId(String classId) throws ExecutionException, InterruptedException {
        List<Map<String, Object>> students = new ArrayList<>();
        List<QueryDocumentSnapshot> relations = firestore.collection("student_class")
                .whereEqualTo("class_id", classId)
                .whereEqualTo("deleted", false).get().get().getDocuments();

        for (QueryDocumentSnapshot rel : relations) {
            String studentId = rel.getString("student_id");
            if (studentId != null) {
                DocumentSnapshot studentDoc = firestore.collection("students").document(studentId).get().get();
                if (studentDoc.exists()) {
                    Map<String, Object> data = studentDoc.getData();
                    data.put("id", studentDoc.getId());
                    students.add(data);
                }
            }
        }
        return students;
    }

    // LẤY DANH SÁCH GIẢNG VIÊN (Từ bảng trung gian teacher_class)
    public List<Map<String, Object>> getTeachersByClassId(String classId) throws ExecutionException, InterruptedException {
        List<Map<String, Object>> teachers = new ArrayList<>();
        List<QueryDocumentSnapshot> relations = firestore.collection("teacher_class")
                .whereEqualTo("class_id", classId)
                .whereEqualTo("deleted", false).get().get().getDocuments();

        for (QueryDocumentSnapshot rel : relations) {
            String teacherId = rel.getString("teacher_id");
            if (teacherId != null) {
                DocumentSnapshot teacherDoc = firestore.collection("teachers").document(teacherId).get().get();
                if (teacherDoc.exists()) {
                    Map<String, Object> data = teacherDoc.getData();
                    data.put("id", teacherDoc.getId());
                    teachers.add(data);
                }
            }
        }
        return teachers;
    }

    // UPDATE
    public String update(String classId, ClassEntity updatedEntity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getClassCollection().document(classId);
        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            transaction.set(docRef, updatedEntity, SetOptions.merge());
            return "Cập nhật thành công Lớp: " + classId;
        });
        return transactionResult.get();
    }

    // DELETE SOFT KÈM CASCADE XÓA BẢNG TRUNG GIAN (Tối ưu bằng WriteBatch)
    public String softDeleteWithCascade(String classId, String deletedBy) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        LocalDateTime now = LocalDateTime.now();
        Date dateNow = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        // 1. Xóa Lớp
        DocumentReference classRef = getClassCollection().document(classId);
        batch.update(classRef, "deleted", true, "updateDate", dateNow, "updateBy", deletedBy);

        // 2. Xóa liên kết Sinh Viên - Lớp
        List<QueryDocumentSnapshot> studentClasses = firestore.collection("student_class")
                .whereEqualTo("class_id", classId).whereEqualTo("deleted", false).get().get().getDocuments();
        for (QueryDocumentSnapshot doc : studentClasses) {
            batch.update(doc.getReference(), "deleted", true, "updateDate", now, "updateBy", deletedBy);
        }

        // 3. Xóa liên kết Giảng Viên - Lớp
        List<QueryDocumentSnapshot> teacherClasses = firestore.collection("teacher_class")
                .whereEqualTo("class_id", classId).whereEqualTo("deleted", false).get().get().getDocuments();
        for (QueryDocumentSnapshot doc : teacherClasses) {
            batch.update(doc.getReference(), "deleted", true, "updateDate", now, "updateBy", deletedBy);
        }

        batch.commit().get(); // Thực thi đồng loạt
        return "Đã xóa mềm Lớp học và dọn dẹp các liên kết liên quan thành công!";
    }
}