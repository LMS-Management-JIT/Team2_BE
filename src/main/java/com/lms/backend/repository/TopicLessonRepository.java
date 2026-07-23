package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.TopicLessonEntity;
import com.lms.backend.mapper.TopicLessonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class TopicLessonRepository {

    private static final String COLLECTION_NAME = "topic_lessons";

    private final TopicLessonMapper topicLessonMapper;
    private final Firestore firestore;

    /**
     * Count by topicId
     */


    public long countByFilters(String topicId) {
        try {
            Query query = firestore.collection("topic_lessons");

             if (StringUtils.hasText(topicId)) {
                query = query.whereEqualTo("topic_id", topicId);
            }

            return query.get().get().size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to count TopicLesson", e);
        }
    }




    //Find By ID
    public TopicLessonEntity findById(String id) throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot = firestore.collection(COLLECTION_NAME).document(id).get().get();

        if (snapshot.exists()) {
            return topicLessonMapper.fromDocument(snapshot);
        }

        return null;
    }




    public Optional<TopicLessonEntity> findByTopicIdAndLessonId(String topicId, String lessonId) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("topic_id", topicId)
                    .whereEqualTo("lesson_id", lessonId)
                    .limit(1);

            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
            if (documents.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(topicLessonMapper.fromDocument(documents.get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find by topic and lesson", e);
        }
    }



    public void updateLessonOrder(String id, Integer newOrder) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .update("lesson_order", newOrder, "updatedAt", Instant.now())
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot update lesson order", e);
        }
    }

    public long countByTopic(String topicId) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("topic_id", topicId);
            return query.get().get().size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot count by topic", e);
        }
    }

    // Helper methods from existing code (preserved style)
    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot delete topic lesson", e);
        }
    }

    public List<TopicLessonEntity> findByLessonId(String lessonId) {
        try {
            List<TopicLessonEntity> list = new ArrayList<>();
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("lesson_id", lessonId);

            for (DocumentSnapshot doc : query.get().get().getDocuments()) {
                list.add(topicLessonMapper.fromDocument(doc));
            }
            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find by lesson id", e);
        }
    }



        public TopicLessonEntity save(TopicLessonEntity entity) {
            try {
                DocumentReference ref;
                if (entity.getId() == null || entity.getId().isEmpty()) {
                    ref = firestore.collection(COLLECTION_NAME).document();
                    entity.setId(ref.getId());
                } else {
                    ref = firestore.collection(COLLECTION_NAME).document(entity.getId());
                }

                ref.set(topicLessonMapper.toFirestoreMap(entity)).get(); // Use mapper if needed
                return entity;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Cannot save topic lesson", e);
            }
        }

        public void delete(String id) {
            try {
                firestore.collection(COLLECTION_NAME).document(id).delete().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Cannot delete topic lesson", e);
            }
        }

        public List<TopicLessonEntity> findByTopicId(String topicId) {
            try {
                Query query = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("topic_id", topicId)
                        .orderBy("lesson_order", Query.Direction.ASCENDING);

                List<TopicLessonEntity> list = new ArrayList<>();
                for (DocumentSnapshot doc : query.get().get().getDocuments()) {
                    list.add(topicLessonMapper.fromDocument(doc));
                }
                return list;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Cannot find by topic id", e);
            }
        }

    public List<TopicLessonEntity> findAllByTopicAndFilters(String topicId, int page, int size) {
        try {
            Query query = firestore.collection(COLLECTION_NAME);

            if (StringUtils.hasText(topicId)) {
                query = query.whereEqualTo("topic_id", topicId);   // Use exact Firestore field name
            }

            query = query.orderBy("lesson_order", Query.Direction.ASCENDING)
                    .offset(page * size)
                    .limit(size + 1);

            List<TopicLessonEntity> list = new ArrayList<>();
            ApiFuture<QuerySnapshot> future = query.get();

            System.out.println("Found documents: " + future.get().size()); // Debug

            for (DocumentSnapshot doc : future.get().getDocuments()) {
                list.add(topicLessonMapper.fromDocument(doc));
            }

            return list;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch TopicLesson by topicId", e);
        }
    }

        public long countByTopicId(String topicId) {
            try {
                Query query = firestore.collection(COLLECTION_NAME);
                if (StringUtils.hasText(topicId)) {
                    query = query.whereEqualTo("topic_id", topicId);
                }
                return query.get().get().size();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to count TopicLesson", e);
            }
        }

        // Helper methods
        public boolean exists(String topicId, String lessonId) {
            try {
                Query query = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("topic_id", topicId)
                        .whereEqualTo("lesson_id", lessonId)
                        .limit(1);
                return !query.get().get().isEmpty();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Cannot check existence", e);
            }
        }
}