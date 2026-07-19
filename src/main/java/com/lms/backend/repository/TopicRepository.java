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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class TopicRepository {

    private final TopicMapper topicMapper;
    private final LessonMapper lessonMapper;
    private final TopicLessonRepository topicLessonRepository;
    private final ModuleTopicRepository moduleTopicRepository;

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


    public List<TopicWithLessonsResponse> getTopicsByModule(String moduleId)
            throws ExecutionException, InterruptedException {

        List<ModuleTopicEntity> relations = moduleTopicRepository.findByModuleId(moduleId);

        List<TopicWithLessonsResponse> result = new ArrayList<>();

        for (ModuleTopicEntity relation : relations) {
            if (Boolean.TRUE.equals(relation.getDeleted())) {
                continue;
            }

            DocumentSnapshot topicDoc = getTopicCollection()
                    .document(relation.getTopicId())
                    .get()
                    .get();

            if (topicDoc.exists()) {
                TopicEntity topicEntity = topicMapper.fromDocument(topicDoc);

                if (topicEntity != null && !topicEntity.isDeleted()) {
                    TopicWithLessonsResponse topicWithLessons = topicMapper.toWithLessonsResponse(topicEntity);
                    List<LessonSummaryResponse> lessons = getLessonsByTopic(relation.getTopicId());
                    topicWithLessons.setLessons(lessons);

                    result.add(topicWithLessons);
                }
            }
        }

        return result;
    }
    public PagedResponse<TopicEntity> findAllActive(int page, int size, String name, String status) throws ExecutionException, InterruptedException {
        List<TopicEntity> all = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = getTopicCollection().whereEqualTo("deleted", false).get();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            TopicEntity entity = topicMapper.fromDocument(doc);
            if (entity != null) all.add(entity);
        }

        // apply optional server-side filtering (simple contains, case-insensitive)
        List<TopicEntity> filtered = new ArrayList<>();
        for (TopicEntity e : all) {
            boolean matches = true;
            if (name != null && !name.isBlank()) {
                String en = e.getTopicName() == null ? "" : e.getTopicName();
                if (!en.toLowerCase().contains(name.toLowerCase())) matches = false;
            }
            // status field not present on TopicEntity; ignore status filter for now
            if (matches) filtered.add(e);
        }

        long total = filtered.size();
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        List<TopicEntity> pageItems = new ArrayList<>();
        if (fromIndex < filtered.size()) {
            pageItems = filtered.subList(fromIndex, toIndex);
        }

        int totalPages = (int) ((total + safeSize - 1) / safeSize);

        return new PagedResponse<>(pageItems, total, safePage, safeSize, totalPages);
    }

    public TopicWithLessonsResponse getTopicDetail(String topicId)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot document = getTopicCollection()
                .document(topicId)
                .get()
                .get();

        TopicEntity topicEntity =
                topicMapper.fromDocument(document);

        if (topicEntity == null || topicEntity.isDeleted()) {
            throw new RuntimeException("Không tìm thấy Topic");
        }

        TopicWithLessonsResponse response =
                topicMapper.toWithLessonsResponse(topicEntity);

        response.setLessons(getLessonsByTopic(topicId));

        return response;
    }

    public List<LessonSummaryResponse> getLessonsByTopic(String topicId)
            throws ExecutionException, InterruptedException {

        List<TopicLessonEntity> relations = topicLessonRepository.findByTopicId(topicId);

        List<LessonSummaryResponse> result = new ArrayList<>();

        for (TopicLessonEntity relation : relations) {
            if (Boolean.TRUE.equals(relation.getDeleted())) {
                continue;
            }

            DocumentSnapshot lessonDoc = getLessonCollection()
                    .document(relation.getLessonId())
                    .get()
                    .get();

            if (lessonDoc.exists()) {
                LessonEntity lessonEntity = lessonMapper.fromDocument(lessonDoc);

                if (lessonEntity != null && !lessonEntity.isDeleted()) {
                    result.add(topicMapper.toLessonSummaryResponse(lessonEntity));
                }
            }
        }

        return result;
    }
}