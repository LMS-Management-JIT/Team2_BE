package com.lms.backend.mapper;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.entity.ClassEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class ClassMapper {

    // 1. Nhận Request từ Frontend -> Chuyển thành Entity để lưu DB
    public ClassEntity toEntity(ClassRequest request) {
        if (request == null) return null;

        ClassEntity entity = new ClassEntity();
        entity.setClassName(request.getClassName());
        entity.setDescription(request.getDescription());
        entity.setPrograme(request.getPrograme());
        entity.setStatus(request.getStatus());
        entity.setCourseId(request.getCourseId());

        if (request.getEndDateClass() != null) {
            entity.setEndDateClass(convertToDate(request.getEndDateClass()));
        }
        return entity;
    }

    // 2. Lấy Entity từ DB -> Chuyển thành Response trả về Frontend
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

        if (entity.getEndDateClass() != null) {
            response.setEndDateClass(convertToLocalDateTime(entity.getEndDateClass()));
        }
        if (entity.getCreateDate() != null) {
            response.setCreateDate(convertToLocalDateTime(entity.getCreateDate()));
        }
        if (entity.getUpdateDate() != null) {
            response.setUpdateDate(convertToLocalDateTime(entity.getUpdateDate()));
        }

        return response;
    }

    private LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Date convertToDate(LocalDateTime dateToConvert) {
        return Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }
}