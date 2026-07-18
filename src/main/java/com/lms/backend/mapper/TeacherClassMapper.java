package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.TeacherClassRequest;
import com.lms.backend.entity.TeacherClassEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TeacherClassMapper {

    public TeacherClassEntity toEntity(String classId) {
        return TeacherClassEntity.builder().classId(classId).build();
    }

    public TeacherClassEntity toEntity(TeacherClassRequest request) {
        return TeacherClassEntity.builder().classId(request.getClassId()).build();
    }

    public TeacherClassEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return null;
        }

        return TeacherClassEntity.builder().id(doc.getId()).teacherId(doc.getString("teacher_id")).classId(doc.getString("class_id")).deleted(doc.getBoolean("deleted")).createdBy(doc.getString("created_by")).updatedBy(doc.getString("updated_by")).createDate(toLocalDateTime(doc.getDate("create_date"))).updateDate(toLocalDateTime(doc.getDate("update_date"))).build();
    }

    public Map<String, Object> toFirestoreMap(TeacherClassEntity entity) {
        Map<String, Object> map = new HashMap<>();

        map.put("teacher_id", entity.getTeacherId());
        map.put("class_id", entity.getClassId());
        map.put("deleted", entity.getDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("updated_by", entity.getUpdatedBy());

        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }

    private Date toTimestamp(LocalDateTime time) {
        if (time == null) return null;
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}