package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;
import com.lms.backend.entity.*;
import com.lms.backend.mapper.LessonMapper;
import com.lms.backend.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class TopicRepository {

    private final TopicMapper topicMapper;
    private final LessonMapper lessonMapper;
    private final ModuleTopicRepository moduleTopicRepository;
    private final TopicLessonRepository topicLessonRepository;   // Important for real relationship

    private final Firestore firestore;

    public CollectionReference getTopicCollection() {
        return firestore.collection("topics");
    }

    public CollectionReference getLessonCollection() {
        return firestore.collection("lessons");
    }

    public Firestore getFirestore() {
        return firestore;
    }

    // ====================== TOPIC METHODS ======================

    public List<TopicWithLessonsResponse> getTopicsByModule(String moduleId)
            throws ExecutionException, InterruptedException {

        List<ModuleTopicEntity> relations = moduleTopicRepository.findByModuleId(moduleId);
        List<TopicWithLessonsResponse> result = new ArrayList<>();

        for (ModuleTopicEntity relation : relations) {
            if (Boolean.TRUE.equals(relation.getDeleted())) continue;

            DocumentSnapshot topicDoc = getTopicCollection()
                    .document(relation.getTopicId())
                    .get()
                    .get();

            if (topicDoc.exists()) {
                TopicEntity topicEntity = topicMapper.fromDocument(topicDoc);

                if (topicEntity != null && !topicEntity.isDeleted()) {
                    TopicWithLessonsResponse topicWithLessons = topicMapper.toWithLessonsResponse(topicEntity);
                    List<LessonSummaryResponse> lessons = findLessonsByTopic(relation.getTopicId());
                    topicWithLessons.setLessons(lessons);
                    result.add(topicWithLessons);
                }
            }
        }
        return result;
    }

    public PagedResponse<TopicEntity> findAllActive(int page, int size, String name, String status)
            throws ExecutionException, InterruptedException {

        ApiFuture<QuerySnapshot> future = getTopicCollection()
                .whereEqualTo("deleted", false)
                .get();

        List<TopicEntity> all = new ArrayList<>();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            TopicEntity entity = topicMapper.fromDocument(doc);
            if (entity != null) all.add(entity);
        }

        List<TopicEntity> filtered = new ArrayList<>();
        for (TopicEntity e : all) {
            boolean matches = true;
            if (StringUtils.hasText(name)) {
                String topicName = e.getTopicName() == null ? "" : e.getTopicName();
                if (!topicName.toLowerCase().contains(name.toLowerCase())) {
                    matches = false;
                }
            }
            if (matches) filtered.add(e);
        }

        long total = filtered.size();
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());

        List<TopicEntity> pageItems = (fromIndex < filtered.size())
                ? filtered.subList(fromIndex, toIndex)
                : new ArrayList<>();

        int totalPages = (int) ((total + safeSize - 1) / safeSize);

        return new PagedResponse<>(pageItems, total, safePage, safeSize, totalPages);
    }

    public TopicWithLessonsResponse getTopicDetail(String topicId)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot document = getTopicCollection()
                .document(topicId)
                .get()
                .get();

        TopicEntity topicEntity = topicMapper.fromDocument(document);

        if (topicEntity == null || topicEntity.isDeleted()) {
            throw new RuntimeException("Không tìm thấy Topic");
        }

        TopicWithLessonsResponse response = topicMapper.toWithLessonsResponse(topicEntity);
        response.setLessons(findLessonsByTopic(topicId));

        return response;
    }

    public TopicEntity findActiveById(String topicId) throws ExecutionException, InterruptedException {
            DocumentSnapshot document = getTopicCollection()
                    .document(topicId)
                    .get()
                    .get();

            if (!document.exists()) return null;

            TopicEntity topicEntity = topicMapper.fromDocument(document);
            if (topicEntity == null || topicEntity.isDeleted()) return null;

            return topicEntity;
    }

    // ====================== LESSON METHODS ======================

    /**
     * Paginated + filtered lessons by topic (using TopicLesson mapping)
     */
    public List<LessonEntity> findLessonsByTopic(String topicId, int page, int size, String name, String status) {
        try {
            // Get mappings first
            List<TopicLessonEntity> mappings = topicLessonRepository.findByTopicId(topicId);

            if (mappings.isEmpty()) return new ArrayList<>();

            List<String> lessonIds = mappings.stream()
                    .map(TopicLessonEntity::getLessonId)
                    .toList();

            // Now query lessons
            Query query = getLessonCollection()
                    .whereIn("id", lessonIds.subList(0, Math.min(10, lessonIds.size()))); // Firestore limit for whereIn

            if (StringUtils.hasText(name)) {
                query = query.whereGreaterThanOrEqualTo("lesson_name", name)
                        .whereLessThanOrEqualTo("lesson_name", name + "\uf8ff");
            }

            if (StringUtils.hasText(status)) {
                query = query.whereEqualTo("published", "PUBLISHED".equals(status)); // adjust based on your logic
            }

            query = query.orderBy("create_at", Query.Direction.DESCENDING)
                    .offset(page * size)
                    .limit(size + 1);

            List<LessonEntity> lessons = new ArrayList<>();
            ApiFuture<QuerySnapshot> future = query.get();

            for (DocumentSnapshot doc : future.get().getDocuments()) {
                LessonEntity entity = lessonMapper.fromDocument(doc);
                if (entity != null) lessons.add(entity);
            }

            return lessons;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch lessons by topic: " + topicId, e);
        }
    }

    /**
     * Simple version for summaries (used in getTopicsByModule & getTopicDetail)
     */
    public List<LessonSummaryResponse> findLessonsByTopic(String topicId) {
        try {
            List<TopicLessonEntity> mappings = topicLessonRepository.findByTopicId(topicId);

            if (mappings.isEmpty()) return new ArrayList<>();

            List<String> lessonIds = mappings.stream()
                    .map(TopicLessonEntity::getLessonId)
                    .toList();

            Query query = getLessonCollection()
                    .whereIn("id", lessonIds.subList(0, Math.min(30, lessonIds.size())))
                    .orderBy("create_at", Query.Direction.ASCENDING)
                    .limit(50);

            List<LessonSummaryResponse> summaries = new ArrayList<>();
            ApiFuture<QuerySnapshot> future = query.get();

            for (DocumentSnapshot doc : future.get().getDocuments()) {
                LessonEntity entity = lessonMapper.fromDocument(doc);
                if (entity != null && !entity.isDeleted()) {
                    summaries.add(lessonMapper.toSummaryResponse(entity));
                }
            }
            return summaries;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch lesson summaries for topic: " + topicId, e);
        }
    }

    /**
     * Count total lessons for pagination
     */
    public long countByTopic(String topicId, String name, String status) {
        try {
            List<TopicLessonEntity> mappings = topicLessonRepository.findByTopicId(topicId);
            return mappings.size(); // Simple count from mapping (most accurate)
        } catch (Exception e) {
            throw new RuntimeException("Failed to count lessons for topic: " + topicId, e);
        }
    }
}