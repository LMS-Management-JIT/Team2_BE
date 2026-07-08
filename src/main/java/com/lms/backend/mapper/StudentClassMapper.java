package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.StudentRequest;
import com.lms.backend.entity.StudentClassEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class StudentClassMapper {

    public StudentClassEntity toEntity(String classId) {

        return StudentClassEntity.builder()
                .classId(classId)
                .build();
    }

    public StudentClassEntity fromDocument(DocumentSnapshot doc) {

        if (doc == null || !doc.exists()) {
            return null;
        }

        return StudentClassEntity.builder()
                .id(doc.getId())
                .studentId(doc.getString("student_id"))
                .classId(doc.getString("class_id"))
                .status(doc.getString("status"))
                .deleted(doc.getBoolean("deleted"))
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .createDate(toLocalDateTime(doc.getDate("create_date")))
                .updateDate(toLocalDateTime(doc.getDate("update_date")))
                .build();
    }

    public Map<String, Object> toFirestoreMap(StudentClassEntity entity) {

        Map<String, Object> map = new HashMap<>();

        map.put("student_id", entity.getStudentId());
        map.put("class_id", entity.getClassId());
        map.put("status", entity.getStatus());
        map.put("deleted", entity.getDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("updated_by", entity.getUpdatedBy());

        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }

    private Date toTimestamp(LocalDateTime time) {

        if (time == null) {
            return null;
        }

        return Date.from(
                time.atZone(ZoneId.systemDefault()).toInstant()
        );
    }

    private LocalDateTime toLocalDateTime(Date date) {

        if (date == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                date.toInstant(),
                ZoneId.systemDefault()
        );
    }
}