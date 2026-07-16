package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.TopicRequest;
import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.TopicSummaryResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;
import com.lms.backend.entity.LessonEntity;
import com.lms.backend.entity.TopicEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.threeten.bp.DateTimeUtils.toLocalDateTime;

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

    private Date toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public TopicEntity fromDocument(DocumentSnapshot doc)
        {
        if (doc == null || !doc.exists()) return null;
        TopicEntity topic = TopicEntity.builder()
                .id(doc.getId())
                .topicName(doc.getString("topicName"))
                .topicCode(doc.getString("topicCode"))
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .description(doc.getString("description"))
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .createdAt(toLocalDateTime(doc.getDate("create_date")))
                .updatedAt(toLocalDateTime(doc.getDate("update_date")))
                .build();
        return topic;
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