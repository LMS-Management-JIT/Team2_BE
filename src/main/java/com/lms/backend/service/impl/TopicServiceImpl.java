package com.lms.backend.service.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse; // Dùng class này làm Core lồng nhau
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.entity.ModuleTopicEntity;
import com.lms.backend.entity.TopicEntity;
import com.lms.backend.entity.TopicLessonEntity;
import com.lms.backend.mapper.LessonFirestoreMapper;
import com.lms.backend.mapper.TopicFirestoreMapper;
import com.lms.backend.mapper.TopicMapper;
import com.lms.backend.repository.ModuleTopicRepository;
import com.lms.backend.repository.TopicLessonRepository;
import com.lms.backend.repository.TopicRepository;
import com.lms.backend.service.lTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements lTopicService {

    private final TopicRepository topicRepository;
    private final ModuleTopicRepository moduleTopicRepository;
    private final TopicLessonRepository topicLessonRepository;
    private final TopicMapper topicMapper;
    private final TopicFirestoreMapper topicFirestoreMapper;
    private final LessonFirestoreMapper lessonFirestoreMapper;

    @Override
    public List<TopicWithLessonsResponse> getTopicsByModule(String moduleId)
            throws ExecutionException, InterruptedException {

        List<ModuleTopicEntity> relations = moduleTopicRepository.findByModuleId(moduleId);

        List<TopicWithLessonsResponse> result = new ArrayList<>();

        for (ModuleTopicEntity relation : relations) {
            if (Boolean.TRUE.equals(relation.getDeleted())) {
                continue;
            }

            DocumentSnapshot topicDoc = topicRepository.getTopicCollection()
                    .document(relation.getTopicId())
                    .get()
                    .get();

            if (topicDoc.exists()) {
                TopicEntity topicEntity = topicFirestoreMapper.fromDocument(topicDoc);

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

    @Override
    public List<LessonSummaryResponse> getLessonsByTopic(String topicId)
            throws ExecutionException, InterruptedException {

        List<TopicLessonEntity> relations = topicLessonRepository.findByTopicId(topicId);

        List<LessonSummaryResponse> result = new ArrayList<>();

        for (TopicLessonEntity relation : relations) {
            if (Boolean.TRUE.equals(relation.getDeleted())) {
                continue;
            }

            DocumentSnapshot lessonDoc = topicRepository.getLessonCollection()
                    .document(relation.getLessonId())
                    .get()
                    .get();

            if (lessonDoc.exists()) {
                LessonEntity lessonEntity = lessonFirestoreMapper.fromDocument(lessonDoc);

                if (lessonEntity != null && !lessonEntity.isDeleted()) {
                    result.add(topicMapper.toLessonSummaryResponse(lessonEntity));
                }
            }
        }

        return result;
    }

    @Override
    public TopicWithLessonsResponse getTopicDetail(String topicId)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot document = topicRepository.getTopicCollection()
                .document(topicId)
                .get()
                .get();

        TopicEntity topicEntity =
                topicFirestoreMapper.fromDocument(document);

        if (topicEntity == null || topicEntity.isDeleted()) {
            throw new RuntimeException("Không tìm thấy Topic");
        }

        TopicWithLessonsResponse response =
                topicMapper.toWithLessonsResponse(topicEntity);

        response.setLessons(getLessonsByTopic(topicId));

        return response;
    }
}