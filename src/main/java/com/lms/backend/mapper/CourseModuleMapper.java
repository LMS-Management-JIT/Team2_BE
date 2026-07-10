package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.CourseModuleRequest;
import com.lms.backend.dto.response.CourseModuleResponse;
import com.lms.backend.entity.CourseModuleEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class CourseModuleMapper {

    public CourseModuleEntity toEntity(CourseModuleRequest request) {
        if (request == null) return null;
        return CourseModuleEntity.builder()
                .courseId(request.getCourseId())
                .moduleId(request.getModuleId())
                .deleted(request.getDeleted() != null ? request.getDeleted() : false)
                .build();
    }

    public CourseModuleResponse toResponse(CourseModuleEntity entity) {
        if (entity == null) return null;
        CourseModuleResponse response = new CourseModuleResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    public void updateEntityFromRequest(CourseModuleRequest request, CourseModuleEntity existing) {
        if (request == null || existing == null) return;
        existing.setCourseId(request.getCourseId());
        existing.setModuleId(request.getModuleId());
        if (request.getDeleted() != null) existing.setDeleted(request.getDeleted());
    }

    // =====================================================================
    // MAPPER THỦ CÔNG CHO FIRESTORE (CHỐNG LỖI 500)
    // =====================================================================

    public Map<String, Object> toFirestoreMap(CourseModuleEntity entity) {
        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("course_id", entity.getCourseId());
        map.put("module_id", entity.getModuleId());
        map.put("deleted", entity.getDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("updated_by", entity.getUpdatedBy());

        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }

    public CourseModuleEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        CourseModuleEntity entity = CourseModuleEntity.builder()
                .id(doc.getId())
                .courseId(doc.getString("course_id"))
                .moduleId(doc.getString("module_id"))
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .createDate(toLocalDateTime(doc.getDate("create_date")))
                .updateDate(toLocalDateTime(doc.getDate("update_date")))
                .build();

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