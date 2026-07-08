package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.mapper.ClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class ClassRepository {

    @Autowired
    private Firestore firestore;

    @Autowired
    private ClassMapper classMapper;

    private CollectionReference getClassCollection() {
        return firestore.collection("classes");
    }

    // CREATE
    public String save(ClassEntity entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getClassCollection().document();
        entity.setId(docRef.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToSave = classMapper.toFirestoreMap(entity);
            transaction.set(docRef, dataToSave);
            return docRef.getId();
        });
        return transactionResult.get();
    }

    // READ ALL ACTIVE
    public List<ClassEntity> findAllActive() throws ExecutionException, InterruptedException {
        List<ClassEntity> result = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = getClassCollection().whereEqualTo("deleted", false).get();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            ClassEntity entity = classMapper.fromDocument(doc);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }

    // LẤY THÔNG TIN 1 LỚP HỌC
    public ClassEntity findById(String classId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = getClassCollection().document(classId).get().get();
        if (snapshot.exists()) {
            return classMapper.fromDocument(snapshot);
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
    public List<Map<String, Object>> findAllCourses() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> courses = new ArrayList<>();
        QuerySnapshot snapshot = firestore.collection("courses").whereEqualTo("deleted", false).get().get();

        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId()); // Gán ID tài liệu vào map
            courses.add(data);
        }
        return courses;
    }
    // LẤY DANH SÁCH SINH VIÊN
    public List<QueryDocumentSnapshot> findStudentsRelationByClassId(String classId) throws Exception {
        return firestore.collection("student_class")
                .whereEqualTo("class_id", classId)
                .whereEqualTo("deleted", false)
                .get().get().getDocuments();
    }

    public DocumentSnapshot getStudentDoc(String studentId) throws Exception {
        return firestore.collection("students").document(studentId.trim()).get().get();
    }
    // Lấy quan hệ từ bảng trung gian
    public List<QueryDocumentSnapshot> findTeachersRelationByClassId(String classId) throws Exception {
        return firestore.collection("teacher_class")
                .whereEqualTo("class_id", classId)
                .whereEqualTo("deleted", false)
                .get().get().getDocuments();
    }

    // Lấy thông tin giảng viên
    public DocumentSnapshot getTeacherDoc(String teacherId) throws Exception {
        return firestore.collection("teachers").document(teacherId.trim()).get().get();
    }

    // Lấy thông tin profile
    public DocumentSnapshot getProfileDoc(String profileId) throws Exception {
        return firestore.collection("profiles").document(profileId).get().get();
    }

    // UPDATE
    public String update(String classId, ClassEntity updatedEntity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getClassCollection().document(classId);
        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToUpdate = classMapper.toFirestoreMap(updatedEntity);
            transaction.set(docRef, dataToUpdate, SetOptions.merge());
            return "Cập nhật thành công Lớp: " + classId;
        });
        return transactionResult.get();
    }

    // DELETE SOFT KÈM CASCADE XÓA BẢNG TRUNG GIAN (Tối ưu bằng WriteBatch)
    public String softDeleteWithCascade(String classId, String deletedBy) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();

        Date dateNow = new Date();

        DocumentReference classRef = getClassCollection().document(classId);
        batch.update(classRef, "deleted", true, "update_date", dateNow, "update_by", deletedBy);

        List<QueryDocumentSnapshot> studentClasses = firestore.collection("student_class")
                .whereEqualTo("class_id", classId).whereEqualTo("deleted", false).get().get().getDocuments();
        for (QueryDocumentSnapshot doc : studentClasses) {
            batch.update(doc.getReference(), "deleted", true, "update_date", dateNow, "update_by", deletedBy);
        }

        List<QueryDocumentSnapshot> teacherClasses = firestore.collection("teacher_class")
                .whereEqualTo("class_id", classId).whereEqualTo("deleted", false).get().get().getDocuments();
        for (QueryDocumentSnapshot doc : teacherClasses) {
            batch.update(doc.getReference(), "deleted", true, "update_date", dateNow, "update_by", deletedBy);
        }

        batch.commit().get();
        return "Đã xóa mềm Lớp học và dọn dẹp các liên kết liên quan thành công!";
    }

    public long countActive() throws Exception {
        return getClassCollection()
                .whereEqualTo("deleted", false)
                .get().get()
                .size();
    }
}