package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.mapper.LessonFirestoreMapper;
import com.lms.backend.mapper.LessonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class LessonRepository {

    private static final String COLLECTION_NAME = "lessons";

    private final Firestore firestore;

    private final LessonMapper lessonMapper;

    private final LessonFirestoreMapper lessonFirestoreMapper;

    public CollectionReference getLessonCollection() {
        return firestore.collection("lessons");
    }

    private Firestore firestore() {
        return firestore;
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

            ref.set(lessonFirestoreMapper.toMap(lesson)).get();

            return lesson;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot save lesson", e);
        }
    }

    // GET ALL
    public PagedResponse<LessonEntity> findAll(int page, int size, String name, String status) throws ExecutionException, InterruptedException {
        List<LessonEntity> all = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = getLessonCollection().whereEqualTo("deleted", false).get();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            LessonEntity entity = lessonMapper.fromDocument(doc);
            if (entity != null) all.add(entity);
        }

        // apply optional server-side filtering (simple contains, case-insensitive)
        List<LessonEntity> filtered = new ArrayList<>();
        for (LessonEntity e : all) {
            boolean matches = true;
            if (name != null && !name.isBlank()) {
                String en = e.getLessonName() == null ? "" : e.getLessonName();
                if (!en.toLowerCase().contains(name.toLowerCase())) matches = false;
            }
            // status field not present on LessonEntity; ignore status filter for now
            if (matches) filtered.add(e);
        }

        long total = filtered.size();
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        List<LessonEntity> pageItems = new ArrayList<>();
        if (fromIndex < filtered.size()) {
            pageItems = filtered.subList(fromIndex, toIndex);
        }

        int totalPages = (int) ((total + safeSize - 1) / safeSize);

        return new PagedResponse<>(pageItems, total, safePage, safeSize, totalPages);
    }

    // FIND BY ID (any)
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

            return lessonMapper.fromDocument(snapshot);

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find lesson", e);
        }
    }

    // FIND BY ID but only if not deleted
    public LessonEntity findActiveById(String id) {
        try {
            DocumentSnapshot snapshot = firestore()
                    .collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();

            if (!snapshot.exists()) return null;

            LessonEntity entity = lessonMapper.fromDocument(snapshot);
            if (entity == null || entity.isDeleted()) return null;
            return entity;
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
                lessons.add(lessonMapper.fromDocument(doc));
            }

            return lessons;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Cannot find lessons by course", e);
        }
    }
}