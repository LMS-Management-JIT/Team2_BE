package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.entity.StudentClassEntity;
import com.lms.backend.mapper.ClassMapper;
import com.lms.backend.mapper.StudentClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class StudentClassRepository {

    @Autowired
    private Firestore firestore;

    @Autowired
    private StudentClassMapper studentClassMapper;

    private final ClassMapper classMapper;

    @Autowired
    private ClassRepository classRepository;



    public CollectionReference getStudentClassCollection() {
        return firestore.collection("student_class");
    }

    // CREATE
    public String save(StudentClassEntity entity)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                getStudentClassCollection().document();

        entity.setId(docRef.getId());

        ApiFuture<String> future =
                firestore.runTransaction(transaction -> {

                    transaction.set(
                            docRef,
                            studentClassMapper.toFirestoreMap(entity)
                    );

                    return docRef.getId();
                });

        return future.get();
    }

    // READ ONE
    public StudentClassEntity findById(String id)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot =
                getStudentClassCollection()
                        .document(id)
                        .get()
                        .get();

        if (snapshot.exists()) {
            return studentClassMapper.fromDocument(snapshot);
        }

        return null;
    }

    // READ BY STUDENT
    public List<StudentClassEntity> findByStudentId(String studentId) {

        try {

            List<StudentClassEntity> result = new ArrayList<>();

            List<QueryDocumentSnapshot> docs =
                    getStudentClassCollection()
                            .whereEqualTo("student_id", studentId)
                            .whereEqualTo("deleted", false)
                            .get()
                            .get()
                            .getDocuments();

            for (QueryDocumentSnapshot doc : docs) {

                StudentClassEntity entity =
                        studentClassMapper.fromDocument(doc);

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

    public List<String> findClassNamesByStudentId(String studentId) {

        try {

            List<String> result = new ArrayList<>();

            List<QueryDocumentSnapshot> relations =
                    getStudentClassCollection()
                            .whereEqualTo("student_id", studentId)
                            .whereEqualTo("deleted", false)
                            .get()
                            .get()
                            .getDocuments();

            for (QueryDocumentSnapshot relation : relations) {

                String classId = relation.getString("class_id");

                if (classId == null || classId.isBlank()) {
                    continue;
                }

                DocumentSnapshot classDoc =
                        firestore.collection("classes")
                                .document(classId)
                                .get()
                                .get();

                if (classDoc.exists()) {
                    result.add(classDoc.getString("class_name"));
                }
            }

            return result;

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách lớp", e);
        }
    }
    // UPDATE
    public String update(StudentClassEntity entity)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                getStudentClassCollection()
                        .document(entity.getId());

        ApiFuture<String> future =
                firestore.runTransaction(transaction -> {

                    transaction.set(
                            docRef,
                            studentClassMapper.toFirestoreMap(entity),
                            SetOptions.merge()
                    );

                    return "Cập nhật StudentClass thành công!";
                });

        return future.get();
    }

    // DELETE
    public String softDelete(StudentClassEntity entity)
            throws ExecutionException, InterruptedException {

        entity.setDeleted(true);

        return update(entity);
    }
}