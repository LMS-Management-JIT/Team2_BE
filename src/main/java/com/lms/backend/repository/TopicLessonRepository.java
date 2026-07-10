package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.lms.backend.entity.TopicLessonEntity;
import com.lms.backend.mapper.TopicLessonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class TopicLessonRepository {

    private static final String COLLECTION_NAME = "topic_lessons";

    private final TopicLessonMapper topicLessonMapper;

    private Firestore firestore() {
        return FirestoreClient.getFirestore();
    }

    // CREATE / UPDATE
    public TopicLessonEntity save(TopicLessonEntity entity) {

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

            ref.set(topicLessonMapper.toFirestoreMap(entity)).get();

            return entity;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot save topic lesson", e);
        }
    }

    // FIND ALL
    public List<TopicLessonEntity> findAll() {

        try {

            List<TopicLessonEntity> list = new ArrayList<>();

            ApiFuture<QuerySnapshot> future = firestore()
                    .collection(COLLECTION_NAME)
                    .get();

            for (DocumentSnapshot doc : future.get().getDocuments()) {
                list.add(topicLessonMapper.fromDocument(doc));
            }

            return list;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot get topic lessons", e);
        }
    }

    // FIND BY ID
    public TopicLessonEntity findById(String id) {

        try {

            DocumentSnapshot snapshot = firestore()
                    .collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();

            if (!snapshot.exists()) {
                return null;
            }

            return topicLessonMapper.fromDocument(snapshot);

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find topic lesson", e);
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
            throw new RuntimeException("Cannot delete topic lesson", e);
        }
    }

    // FIND BY TOPIC ID
    public List<TopicLessonEntity> findByTopicId(String topicId) {

        try {

            List<TopicLessonEntity> list = new ArrayList<>();

            Query query = firestore()
                    .collection(COLLECTION_NAME)
                    .whereEqualTo("topic_id", topicId);

            for (DocumentSnapshot doc : query.get().get().getDocuments()) {
                list.add(topicLessonMapper.fromDocument(doc));
            }

            return list;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find by topic id", e);
        }
    }

    // FIND BY LESSON ID
    public List<TopicLessonEntity> findByLessonId(String lessonId) {

        try {

            List<TopicLessonEntity> list = new ArrayList<>();

            Query query = firestore()
                    .collection(COLLECTION_NAME)
                    .whereEqualTo("lesson_id", lessonId);

            for (DocumentSnapshot doc : query.get().get().getDocuments()) {
                list.add(topicLessonMapper.fromDocument(doc));
            }

            return list;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find by lesson id", e);
        }
    }
}