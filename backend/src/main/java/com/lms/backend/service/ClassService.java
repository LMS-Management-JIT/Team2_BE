package com.lms.backend.service;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.mapper.ClassMapper;
import com.lms.backend.repository.ClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ClassService {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassMapper classMapper;

    // CREATE
    public String createClass(ClassRequest request) throws ExecutionException, InterruptedException {
        // 1. Business Logic & Mapping
        ClassEntity entity = classMapper.toEntity(request);
        entity.setCreateDate(new Date());
        entity.setCreatedBy(request.getRequestedBy());
        entity.setDeleted(false);

        // 2. Gọi Repository
        return classRepository.save(entity);
    }

    // READ
    public List<ClassResponse> getAllClasses() throws ExecutionException, InterruptedException {
        // 1. Lấy Entities từ DB
        List<ClassEntity> entities = classRepository.findAllActive();

        // 2. Map sang Response DTO
        return entities.stream()
                .map(classMapper::toResponse)
                .collect(Collectors.toList());
    }

    // UPDATE
    public String updateClass(String classId, ClassRequest request) throws Exception {
        // 1. Validate nghiệp vụ
        ClassEntity existingClass = classRepository.findById(classId);
        if (existingClass == null) {
            throw new Exception("Lớp học không tồn tại, không thể cập nhật!");
        }

        // 2. Cập nhật Entity
        ClassEntity updatedEntity = classMapper.toEntity(request);
        updatedEntity.setUpdateDate(new Date());
        updatedEntity.setUpdateBy(request.getRequestedBy());

        // 3. Lưu qua Repo
        return classRepository.update(classId, updatedEntity);
    }

    // DELETE SOFT
    public String deleteClass(String classId, String deletedBy) throws Exception {
        // 1. Validate
        ClassEntity existingClass = classRepository.findById(classId);
        if (existingClass == null) {
            throw new Exception("Lớp học không tồn tại!");
        }

        // 2. Chuẩn bị data update
        Map<String, Object> updates = new HashMap<>();
        updates.put("deleted", true);
        updates.put("updateDate", new Date());
        updates.put("updateBy", deletedBy);

        // 3. Thực thi qua repo
        return classRepository.softDelete(classId, updates);
    }
}