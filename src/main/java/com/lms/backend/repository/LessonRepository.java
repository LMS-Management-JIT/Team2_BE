package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.mapper.LessonFirestoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class LessonRepository {

    private static final String COLLECTION_NAME = "lessons";

    private final LessonFirestoreMapper firestoreMapper;

    private Firestore firestore() {
        return FirestoreClient.getFirestore();
    }

    // CREATE / UPDATE
    public LessonEntity save(LessonEntity lesson) {

        try {

            DocumentReference ref;

            if (lesson.getId() == null) {

                ref = firestore()
                        .collection(COLLECTION_NAME)
                        .document();

                lesson.setId(ref.getId());

            } else {

                ref = firestore()
                        .collection(COLLECTION_NAME)
                        .document(lesson.getId());
            }

            ref.set(firestoreMapper.toMap(lesson)).get();

            return lesson;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot save lesson", e);
        }
    }

    // GET ALL
    public List<LessonEntity> findAll() {

        try {

            List<LessonEntity> lessons = new ArrayList<>();

            ApiFuture<QuerySnapshot> future = firestore()
                    .collection(COLLECTION_NAME)
                    .get();

            for (DocumentSnapshot doc : future.get().getDocuments()) {
                lessons.add(firestoreMapper.fromDocument(doc));
            }

            return lessons;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot get lessons", e);
        }
    }

    // FIND BY ID
    public LessonEntity findById(String id) {

        try {

            DocumentSnapshot snapshot = firestore()
                    .collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();

            if (!snapshot.exists()) {
                return null;
            }

            return firestoreMapper.fromDocument(snapshot);

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find lesson", e);
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
            throw new RuntimeException("Cannot delete lesson", e);
        }
    }

    // FIND BY COURSE ID
    public List<LessonEntity> findByCourseId(String courseId) {

        try {

            List<LessonEntity> lessons = new ArrayList<>();

            Query query = firestore()
                    .collection(COLLECTION_NAME)
                    .whereEqualTo("courseId", courseId);

            for (DocumentSnapshot doc : query.get().get().getDocuments()) {
                lessons.add(firestoreMapper.fromDocument(doc));
            }

            return lessons;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find lessons by course", e);
        }
    }
}