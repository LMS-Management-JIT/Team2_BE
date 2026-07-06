package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.entity.ClassEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClassMapper {

    public ClassEntity toEntity(ClassRequest request) {
        if (request == null) return null;

        return ClassEntity.builder()
                .className(request.getClassName())
                .description(request.getDescription())
                .programe(request.getPrograme())
                .status(request.getStatus())
                .courseId(request.getCourseId())
                .startDateClass(request.getStartDateClass())
                .endDateClass(request.getEndDateClass())
                .build();
    }

    public ClassResponse toResponse(ClassEntity entity) {
        if (entity == null) return null;

        ClassResponse response = new ClassResponse();
        response.setId(entity.getId());
        response.setClassName(entity.getClassName());
        response.setDescription(entity.getDescription());
        response.setPrograme(entity.getPrograme());
        response.setStatus(entity.getStatus());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdateBy(entity.getUpdateBy());
        response.setDeleted(entity.isDeleted());
        response.setCourseId(entity.getCourseId());
        response.setStartDateClass(entity.getStartDateClass());
        response.setEndDateClass(entity.getEndDateClass());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());

        return response;
    }

    public void updateEntityFromRequest(ClassRequest request, ClassEntity existingEntity) {
        if (request == null || existingEntity == null) return;

        existingEntity.setClassName(request.getClassName());
        existingEntity.setDescription(request.getDescription());
        existingEntity.setPrograme(request.getPrograme());
        existingEntity.setStatus(request.getStatus());
        existingEntity.setCourseId(request.getCourseId());
        existingEntity.setStartDateClass(request.getStartDateClass());
        existingEntity.setEndDateClass(request.getEndDateClass());
    }

    public Map<String, Object> toFirestoreMap(ClassEntity entity) {
        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("class_name", entity.getClassName());
        map.put("description", entity.getDescription());
        map.put("programe", entity.getPrograme());
        map.put("status", entity.getStatus());
        map.put("deleted", entity.isDeleted());
        map.put("course_id", entity.getCourseId());
        map.put("created_by", entity.getCreatedBy());
        map.put("update_by", entity.getUpdateBy());

        // Đổi LocalDateTime thành Date cho Firestore
        map.put("start_date_class", toTimestamp(entity.getStartDateClass()));
        map.put("end_date_class", toTimestamp(entity.getEndDateClass()));
        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }

    public ClassEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        ClassEntity entity = doc.toObject(ClassEntity.class);

        if (entity != null) {
            entity.setId(doc.getId());
            if (doc.contains("class_name")) {
                entity.setClassName(doc.getString("class_name"));
            }
            if (doc.contains("course_id")) {
                entity.setCourseId(doc.getString("course_id"));
            }
            if (doc.contains("created_by")) {
                entity.setCreatedBy(doc.getString("created_by"));
            }
            if (doc.contains("update_by")) {
                entity.setUpdateBy(doc.getString("update_by"));
            }
            entity.setStartDateClass(toLocalDateTime(doc.getDate("start_date_class")));
            entity.setEndDateClass(toLocalDateTime(doc.getDate("end_date_class")));
            entity.setCreateDate(toLocalDateTime(doc.getDate("create_date")));
            entity.setUpdateDate(toLocalDateTime(doc.getDate("update_date")));
        }

        return entity;
    }

    private Date toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}