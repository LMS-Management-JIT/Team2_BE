package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.lms.backend.entity.LessonProgressEntity;
import com.lms.backend.mapper.LessonProgressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class LessonProgressRepository {

    private static final String COLLECTION_NAME = "lesson_progress";
    private final LessonProgressMapper lessonProgressMapper;

    private Firestore firestore() {
        return FirestoreClient.getFirestore();
    }

    // CREATE / UPDATE
    public LessonProgressEntity save(LessonProgressEntity entity) {

        try {

            DocumentReference ref;

            if (entity.getId() == null) {

                ref = firestore()
                        .collection(COLLECTION_NAME)
                        .document();

                entity.setId(ref.getId());

            } else {
                ref = firestore()
                        .collection(COLLECTION_NAME)
                        .document(entity.getId());
            }
            ref.set(lessonProgressMapper.toFirestoreMap(entity)).get();
            return entity;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot save lesson progress", e);
        }
    }

    // GET ALL
    public List<LessonProgressEntity> findAll() {

        try {

            List<LessonProgressEntity> list = new ArrayList<>();

            ApiFuture<QuerySnapshot> future = firestore()
                    .collection(COLLECTION_NAME)
                    .get();

            for (DocumentSnapshot doc : future.get().getDocuments()) {
                list.add(lessonProgressMapper.fromDocument(doc));
            }

            return list;

        } catch (InterruptedException | ExecutionException e) {

            throw new RuntimeException("Cannot get lesson progress", e);
        }
    }

    // FIND BY ID
    public LessonProgressEntity findById(String id) {

        try {

            DocumentSnapshot snapshot = firestore()
                    .collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();

            if (!snapshot.exists()) {
                return null;
            }
            return lessonProgressMapper.fromDocument(snapshot);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find lesson progress", e);
        }
    }

    // DELETE
    public void deleteById(String id) {

        try {

            firestore()
                    .collection(COLLECTION_NAME)
                    .document(id)
                    .delete()
                    .get();

        } catch (InterruptedException | ExecutionException e) {

            throw new RuntimeException("Cannot delete lesson progress", e);
        }
    }

    // FIND BY STUDENT ID
    public List<LessonProgressEntity> findByStudentId(String studentId) {

        try {

            List<LessonProgressEntity> list = new ArrayList<>();

            Query query = firestore()
                    .collection(COLLECTION_NAME)
                    .whereEqualTo("student_id", studentId);

            for (DocumentSnapshot doc : query.get().get().getDocuments()) {
                list.add(lessonProgressMapper.fromDocument(doc));
            }

            return list;

        } catch (InterruptedException | ExecutionException e) {

            throw new RuntimeException("Cannot find lesson progress by student", e);
        }
    }

    // FIND BY STUDENT + LESSON
    public LessonProgressEntity findByStudentAndLesson(
            String studentId,
            String lessonId
    ) {

        try {

            QuerySnapshot snapshot = firestore()
                    .collection(COLLECTION_NAME)
                    .whereEqualTo("student_id", studentId)
                    .whereEqualTo("lesson_id", lessonId)
                    .get()
                    .get();

            if (snapshot.isEmpty()) {
                return null;
            }

            return lessonProgressMapper.fromDocument(snapshot.getDocuments().get(0));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find lesson progress", e);
        }
    }
}