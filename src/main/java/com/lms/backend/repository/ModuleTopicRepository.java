package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.lms.backend.entity.ModuleTopicEntity;
import com.lms.backend.mapper.ModuleTopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class ModuleTopicRepository {

    private static final String COLLECTION_NAME = "module_topics";

    private final ModuleTopicMapper moduleTopicMapper;

    private Firestore firestore() {
        return FirestoreClient.getFirestore();
    }

    // CREATE / UPDATE
    public ModuleTopicEntity save(ModuleTopicEntity entity) {
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

            ref.set(moduleTopicMapper.toFirestoreMap(entity)).get();
            return entity;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot save module topic", e);
        }
    }

    // FIND ALL
    public List<ModuleTopicEntity> findAll() {
        try {
            List<ModuleTopicEntity> list = new ArrayList<>();

            ApiFuture<QuerySnapshot> future = firestore()
                    .collection(COLLECTION_NAME)
                    .get();

            for (DocumentSnapshot doc : future.get().getDocuments()) {
                list.add(moduleTopicMapper.fromDocument(doc));
            }

            return list;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot get module topics", e);
        }
    }

    // FIND BY ID
    public ModuleTopicEntity findById(String id) {
        try {
            DocumentSnapshot snapshot = firestore()
                    .collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();

            if (!snapshot.exists()) {
                return null;
            }

            return moduleTopicMapper.fromDocument(snapshot);

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find module topic", e);
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
            throw new RuntimeException("Cannot delete module topic", e);
        }
    }

    // FIND BY MODULE ID
    public List<ModuleTopicEntity> findByModuleId(String moduleId) {
        try {
            List<ModuleTopicEntity> list = new ArrayList<>();

            Query query = firestore()
                    .collection(COLLECTION_NAME)
                    .whereEqualTo("module_id", moduleId);

            for (DocumentSnapshot doc : query.get().get().getDocuments()) {
                list.add(moduleTopicMapper.fromDocument(doc));
            }

            return list;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find by module id", e);
        }
    }

    // FIND BY TOPIC ID
    public List<ModuleTopicEntity> findByTopicId(String topicId) {
        try {
            List<ModuleTopicEntity> list = new ArrayList<>();

            Query query = firestore()
                    .collection(COLLECTION_NAME)
                    .whereEqualTo("topic_id", topicId);

            for (DocumentSnapshot doc : query.get().get().getDocuments()) {
                list.add(moduleTopicMapper.fromDocument(doc));
            }

            return list;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find by topic id", e);
        }
    }

    // FIND BY MODULE ID + TOPIC ID
    public java.util.Optional<ModuleTopicEntity> findByModuleIdAndTopicId(String moduleId, String topicId) {
        try {
            Query query = firestore()
                    .collection(COLLECTION_NAME)
                    .whereEqualTo("module_id", moduleId)
                    .whereEqualTo("topic_id", topicId)
                    .limit(1);

            List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();
            if (docs.isEmpty()) return java.util.Optional.empty();

            return java.util.Optional.of(moduleTopicMapper.fromDocument(docs.get(0)));

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find by module id and topic id", e);
        }
    }

    // CHECK EXISTS
    public boolean exists(String moduleId, String topicId) {
        return findByModuleIdAndTopicId(moduleId, topicId).isPresent();
    }

    // COUNT BY MODULE ID
    public long countByModuleId(String moduleId) {
        try {
            Query query = firestore()
                    .collection(COLLECTION_NAME)
                    .whereEqualTo("module_id", moduleId);

            return query.get().get().size();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot count by module id", e);
        }
    }

    // UPDATE TOPIC ORDER
    public void updateTopicOrder(String id, Integer newOrder) {
        try {
            firestore()
                    .collection(COLLECTION_NAME)
                    .document(id)
                    .update("topic_order", newOrder)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update topic order", e);
        }
    }
}