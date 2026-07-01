package com.lms.backend.service;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassDetailResponse;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.mapper.ClassMapper;
import com.lms.backend.repository.ClassRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ClassService implements IClassService {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassMapper classMapper;

    @Override
    public String createClass(ClassRequest request) throws ExecutionException, InterruptedException {
        ClassEntity entity = classMapper.toEntity(request);
        entity.setStartDateClass(request.getStartDateClass());
        entity.setCreateDate(LocalDateTime.now());
        entity.setCreatedBy(request.getRequestedBy());
        entity.setDeleted(false);
        return classRepository.save(entity);
    }

    @Override
    public List<ClassResponse> getAllClasses() throws ExecutionException, InterruptedException {
        List<ClassEntity> entities = classRepository.findAllActive();
        return entities.stream().map(classMapper::toResponse).collect(Collectors.toList());
    }

    // READ DETAIL
    @Override
    public ClassDetailResponse getClassDetail(String classId) throws ExecutionException, InterruptedException {
        ClassEntity classEntity = classRepository.findById(classId);
        if (classEntity == null || classEntity.isDeleted()) {
            throw new IllegalArgumentException("Lớp học không tồn tại hoặc đã bị xóa!");
        }

        ClassResponse baseResponse = classMapper.toResponse(classEntity);
        ClassDetailResponse detailResponse = new ClassDetailResponse();

        // Copy nhanh các trường cơ bản từ base sang detail
        BeanUtils.copyProperties(baseResponse, detailResponse);

        // Fetch dữ liệu liên quan từ các Collection khác
        detailResponse.setCourse(classRepository.getCourseById(classEntity.getCourseId()));
        detailResponse.setStudents(classRepository.getStudentsByClassId(classId));
        detailResponse.setTeachers(classRepository.getTeachersByClassId(classId));

        return detailResponse;
    }

    @Override
    public String updateClass(String classId, ClassRequest request) throws ExecutionException, InterruptedException {
        ClassEntity existingClass = classRepository.findById(classId);
        if (existingClass == null || existingClass.isDeleted()) {
            throw new IllegalArgumentException("Lớp học không tồn tại, không thể cập nhật!");
        }

        classMapper.updateEntityFromRequest(request, existingClass);
        existingClass.setUpdateDate(LocalDateTime.now());
        existingClass.setUpdateBy(request.getRequestedBy());

        return classRepository.update(classId, existingClass);
    }

    @Override
    public String deleteClass(String classId, String deletedBy) throws ExecutionException, InterruptedException {
        ClassEntity existingClass = classRepository.findById(classId);
        if (existingClass == null || existingClass.isDeleted()) {
            throw new IllegalArgumentException("Lớp học không tồn tại hoặc đã xóa!");
        }

        // Gọi hàm xóa dây chuyền sử dụng WriteBatch
        return classRepository.softDeleteWithCascade(classId, deletedBy);
    }

    @Override
    public long countClasses() throws Exception {
        return classRepository.countActive();
    }
}