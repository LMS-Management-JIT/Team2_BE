package com.lms.backend.service.impl;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassDetailResponse;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.ClassMapper;
import com.lms.backend.repository.ClassRepository;
import com.lms.backend.service.IClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements IClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;

    private <T> T executeWithCatch(Callable<T> task, String errorMessage) {
        try {
            return task.call();
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(errorMessage + ": " + e.getMessage(), e);
        }
    }

    private ClassEntity checkClassExists(String classId) {
        return executeWithCatch(() -> {
            ClassEntity classEntity = classRepository.findById(classId);
            if (classEntity == null || classEntity.isDeleted()) {
                throw new ResourceNotFoundException("Lớp học không tồn tại hoặc đã bị xóa!");
            }
            return classEntity;
        }, "Lỗi hệ thống khi kiểm tra dữ liệu lớp học");
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication required");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof String principalName &&
                !"anonymousUser".equals(principalName)) {
            return principalName;
        }

        throw new IllegalStateException("Authentication required");
    }

    private void validateClassLogic(ClassRequest request, boolean isUpdate) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra ngày kết thúc >= ngày bắt đầu
        if (request.getEndDateClass().isBefore(request.getStartDateClass())) {
            throw new BadRequestException("Logic sai: Ngày kết thúc không thể diễn ra trước ngày bắt đầu!");
        }

        // 2. Chặn ngày bắt đầu trong quá khứ khi tạo mới
        if (!isUpdate && request.getStartDateClass().isBefore(now.minusDays(1))) {
            throw new BadRequestException("Logic sai: Lớp học tạo mới không thể bắt đầu từ trong quá khứ!");
        }

        // 3. Ràng buộc thời lượng tối thiểu (Ví dụ: tối thiểu 7 ngày)
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(request.getStartDateClass(), request.getEndDateClass());
        if (daysBetween < 7) {
            throw new BadRequestException("Logic sai: Thời lượng lớp học quá ngắn. Tối thiểu phải là 7 ngày!");
        }

        // 4. Validate giá trị Status hợp lệ
        List<String> validStatuses = List.of("ACTIVE", "INACTIVE", "COMPLETED", "CANCELLED");
        if (request.getStatus() != null && !validStatuses.contains(request.getStatus().toUpperCase())) {
            throw new BadRequestException("Logic sai: Trạng thái không hợp lệ. Chỉ chấp nhận: ACTIVE, INACTIVE, COMPLETED, CANCELLED.");
        }

        // 5. Kiểm tra logic Trạng thái vs Thời gian thực tế
        if ("ACTIVE".equalsIgnoreCase(request.getStatus())) {
            if (now.isBefore(request.getStartDateClass()) || now.isAfter(request.getEndDateClass())) {
                throw new BadRequestException("Logic sai: Không thể set trạng thái ACTIVE khi thời điểm hiện tại không nằm trong khoảng thời gian diễn ra lớp học!");
            }
        }

        if ("COMPLETED".equalsIgnoreCase(request.getStatus()) && request.getEndDateClass().isAfter(now)) {
            throw new BadRequestException("Logic sai: Lớp học chưa đến ngày kết thúc, không thể set trạng thái COMPLETED!");
        }
    }

    @Override
    public String createClass(ClassRequest request) {
        String email = getCurrentUserEmail();
        validateClassLogic(request, false);
        return executeWithCatch(() -> {
            ClassEntity entity = classMapper.toEntity(request);
            entity.setStartDateClass(request.getStartDateClass());
            entity.setCreateDate(LocalDateTime.now());
            entity.setCreatedBy(email);
            entity.setDeleted(false);
            return classRepository.save(entity);
        }, "Lỗi hệ thống khi tạo lớp học");
    }

    @Override
    public List<ClassResponse> getAllClasses() {
        return executeWithCatch(() -> {
            List<ClassEntity> entities = classRepository.findAllActive();
            return entities.stream().map(classMapper::toResponse).collect(Collectors.toList());
        }, "Lỗi hệ thống khi lấy danh sách lớp học");
    }

    @Override
    public ClassDetailResponse getClassDetail(String classId) {
        ClassEntity classEntity = checkClassExists(classId);

        ClassResponse baseResponse = classMapper.toResponse(classEntity);
        ClassDetailResponse detailResponse = new ClassDetailResponse();
        BeanUtils.copyProperties(baseResponse, detailResponse);

        return executeWithCatch(() -> {
            detailResponse.setCourse(classRepository.getCourseById(classEntity.getCourseId()));
            detailResponse.setStudents(classRepository.getStudentsByClassId(classId));
            detailResponse.setTeachers(classRepository.getTeachersByClassId(classId));
            return detailResponse;
        }, "Lỗi hệ thống khi lấy chi tiết liên kết của lớp học");
    }

    @Override
    public Map<String, Object> getCourseOfClass(String classId) {
        ClassEntity classEntity = checkClassExists(classId);
        return executeWithCatch(() -> classRepository.getCourseById(classEntity.getCourseId()),
                "Lỗi hệ thống khi lấy thông tin khóa học");
    }

    @Override
    public List<Map<String, Object>> getStudentsOfClass(String classId) {
        checkClassExists(classId);
        return executeWithCatch(() -> classRepository.getStudentsByClassId(classId),
                "Lỗi hệ thống khi lấy danh sách sinh viên");
    }

    @Override
    public List<Map<String, Object>> getTeachersOfClass(String classId) {
        checkClassExists(classId);
        return executeWithCatch(() -> classRepository.getTeachersByClassId(classId),
                "Lỗi hệ thống khi lấy danh sách giảng viên");
    }

    @Override
    public String updateClass(String classId, ClassRequest request) {
        String email = getCurrentUserEmail();
        ClassEntity existingClass = checkClassExists(classId);
        validateClassLogic(request, true);
        return executeWithCatch(() -> {
            classMapper.updateEntityFromRequest(request, existingClass);
            existingClass.setUpdateDate(LocalDateTime.now());
            existingClass.setUpdateBy(email);
            return classRepository.update(classId, existingClass);
        }, "Lỗi hệ thống khi cập nhật lớp học");
    }

    @Override
    public String deleteClass(String classId, String deletedBy) {
        checkClassExists(classId);
        return executeWithCatch(() -> classRepository.softDeleteWithCascade(classId, deletedBy),
                "Lỗi hệ thống khi xóa lớp học");
    }

    @Override
    public long countClasses() {
        return executeWithCatch(classRepository::countActive,
                "Lỗi hệ thống khi đếm số lượng lớp");
    }
}