package com.lms.backend.mapper;

import com.lms.backend.dto.request.TopicRequest;
import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.TopicSummaryResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.entity.TopicEntity;
import org.springframework.stereotype.Component;

@Component
public class TopicMapper {

    /**
     * TopicRequest -> TopicEntity
     */
    public TopicEntity toEntity(TopicRequest request) {

        if (request == null) {
            return null;
        }

        return TopicEntity.builder()
                .topicName(request.getTopicName())
                .build();
    }

    /**
     * TopicEntity -> TopicSummaryResponse
     */
    public TopicSummaryResponse toSummaryResponse(TopicEntity entity) {

        if (entity == null) {
            return null;
        }

        return TopicSummaryResponse.builder()
                .id(entity.getId())
                .topicName(entity.getTopicName())
                .build();
    }

    /**
     * TopicEntity -> TopicWithLessonsResponse
     */
    public TopicWithLessonsResponse toWithLessonsResponse(TopicEntity entity) {

        if (entity == null) {
            return null;
        }

        return TopicWithLessonsResponse.builder()
                .id(entity.getId())
                .topicName(entity.getTopicName())
                .build();
    }

    /**
     * LessonEntity -> LessonSummaryResponse
     */
    public LessonSummaryResponse toLessonSummaryResponse(LessonEntity lesson) {

        if (lesson == null) {
            return null;
        }

        return LessonSummaryResponse.builder()
                .id(lesson.getId())
                .lessonName(lesson.getLessonName())
                .type(lesson.getType())
                .lessonOrder(lesson.getLessonOrder())
                .duration(lesson.getDuration())
                .published(lesson.getPublished())
                .build();
    }

    /**
     * Update Topic
     */
    public void updateEntity(TopicEntity entity, TopicRequest request) {

        entity.setTopicName(request.getTopicName());
    }

}