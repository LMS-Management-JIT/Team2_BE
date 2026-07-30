package com.lms.backend.mapper;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.CourseRequest;
import com.lms.backend.dto.response.CourseResponse;
import com.lms.backend.entity.CourseEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CourseMapper {

    // 1. Convert Request -> Entity
    public CourseEntity toEntity(CourseRequest request) {
        if (request == null) return null;
        return CourseEntity.builder()
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .deleted(false)
                .build();
    }

    // 2. Convert Entity -> Response
    public CourseResponse toResponse(CourseEntity entity) {
        if (entity == null) return null;
        CourseResponse response = new CourseResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    // 3. Update Entity from Request
    public void updateEntityFromRequest(CourseRequest request, CourseEntity existing) {
        if (request == null || existing == null) return;
        existing.setCourseName(request.getCourseName());
        existing.setDescription(request.getDescription());
        existing.setThumbnailUrl(request.getThumbnailUrl());
    }

    // 4. Convert Entity -> Map để lưu xuống Firestore
    public Map<String, Object> toFirestoreMap(CourseEntity entity) {
        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("courseName", entity.getCourseName());
        map.put("description", entity.getDescription());
        map.put("thumbnailUrl", entity.getThumbnailUrl());
        map.put("deleted", entity.isDeleted());
        map.put("createAt", entity.getCreateAt());
        map.put("updatedAt", entity.getUpdatedAt());
        map.put("createdBy", entity.getCreatedBy());
        map.put("updatedBy", entity.getUpdatedBy());

        return map;
    }

    // 5. Convert Document từ Firestore -> Entity
    public CourseEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        CourseEntity entity = CourseEntity.builder()
                .id(doc.getId())
                .courseName(doc.getString("courseName"))
                .description(doc.getString("description"))
                .thumbnailUrl(doc.getString("thumbnailUrl"))
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .createdBy(doc.getString("createdBy"))
                .updatedBy(doc.getString("updatedBy"))
                .createAt(toTimestamp(doc.get("createAt")))
                .updatedAt(toTimestamp(doc.get("updatedAt")))
                .build();

        return entity;
    }

    // Chuyển đổi Object từ Firestore thành Timestamp (hỗ trợ cả String và Timestamp)
    private Timestamp toTimestamp(Object value) {
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        if (value instanceof String) {
            try {
                java.util.Date date = java.util.Date.from(java.time.Instant.parse((String) value));
                return Timestamp.of(date);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}