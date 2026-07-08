package com.lms.backend.service.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.request.HistoryRequest;
import com.lms.backend.dto.response.ClassDetailResponse;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.entity.ActionType;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.entity.EntityType;
import com.lms.backend.entity.HistoryEntity;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.ClassMapper;
import com.lms.backend.repository.ClassRepository;
import com.lms.backend.service.IClassService;
import com.lms.backend.service.IHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.lms.backend.utils.HistoryChangeUtils.changed;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements IClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final IHistoryService historyService;

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
        String newId = executeWithCatch(() -> {
            ClassEntity entity = classMapper.toEntity(request);
            entity.setStartDateClass(request.getStartDateClass());
            entity.setCreateDate(LocalDateTime.now());
            entity.setCreatedBy(email);
            entity.setDeleted(false);
            return classRepository.save(entity);
        }, "Lỗi hệ thống khi tạo lớp học");

        historyService.record(HistoryRequest.builder()
                .action(ActionType.ADD)
                .actorId(email)
                .actorName(email)
                .entityType(EntityType.CLASS.name())
                .entityId(newId)
                .entityName(request.getClassName())
                .changes(Map.of())
                .build());

        return newId;
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
            detailResponse.setStudents(this.getStudentsOfClass(classId));
            detailResponse.setTeachers(this.getTeachersOfClass(classId));
            return detailResponse;
        }, "Lỗi hệ thống khi lấy chi tiết liên kết của lớp học");
    }

    @Override
    public List<Map<String, Object>> findAllCourses() {
        return executeWithCatch(() -> classRepository.findAllCourses(),
                "Lỗi hệ thống khi lấy danh sách khóa học");
    }

    @Override
    public List<Map<String, Object>> getStudentsOfClass(String classId) {
        try {
            List<Map<String, Object>> students = new ArrayList<>();

            // 1. Lấy quan hệ từ Repository
            List<QueryDocumentSnapshot> relations = classRepository.findStudentsRelationByClassId(classId);

            for (QueryDocumentSnapshot rel : relations) {
                String studentId = rel.getString("student_id");
                if (studentId != null) {
                    // 2. Gọi Repo lấy thông tin Student (để có profile_id)
                    DocumentSnapshot studentDoc = classRepository.getStudentDoc(studentId);

                    if (studentDoc.exists()) {
                        String profileId = studentDoc.getString("profile_id");

                        Map<String, Object> studentInfo = new java.util.HashMap<>();
                        studentInfo.put("id", studentId);
                        studentInfo.put("avatarUrl", "https://via.placeholder.com/100");
                        studentInfo.put("name", "Sinh viên chưa cập nhật");

                        // 3. Gọi Repo lấy Profile nếu có profileId
                        if (profileId != null) {
                            DocumentSnapshot profileDoc = classRepository.getProfileDoc(profileId);
                            if (profileDoc.exists()) {
                                String fullName = profileDoc.getString("full_name");
                                if (fullName != null) {
                                    studentInfo.put("name", fullName);
                                }
                            }
                        }
                        students.add(studentInfo);
                    }
                }
            }
            return students;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách sinh viên: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getTeachersOfClass(String classId) {
        try {
            List<Map<String, Object>> teachers = new ArrayList<>();

            // 1. Lấy quan hệ từ Repository
            List<QueryDocumentSnapshot> relations = classRepository.findTeachersRelationByClassId(classId);

            for (QueryDocumentSnapshot rel : relations) {
                String teacherId = rel.getString("teacher_id");
                if (teacherId != null) {

                    // 2. Gọi Repo lấy thông tin Teacher
                    DocumentSnapshot teacherDoc = classRepository.getTeacherDoc(teacherId);

                    if (teacherDoc.exists()) {
                        String profileId = teacherDoc.getString("profile_id");

                        Map<String, Object> teacherInfo = new java.util.HashMap<>();
                        teacherInfo.put("id", teacherId);
                        teacherInfo.put("avatarUrl", "https://via.placeholder.com/100");
                        teacherInfo.put("name", "Giảng viên chưa cập nhật");

                        // 3. Gọi Repo lấy Profile nếu có profileId
                        if (profileId != null) {
                            DocumentSnapshot profileDoc = classRepository.getProfileDoc(profileId);
                            if (profileDoc.exists()) {
                                String fullName = profileDoc.getString("full_name");
                                if (fullName != null) {
                                    teacherInfo.put("name", fullName);
                                }
                            }
                        }
                        teachers.add(teacherInfo);
                    }
                }
            }
            return teachers;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách giảng viên: " + e.getMessage());
        }
    }

    @Override
    public String updateClass(String classId, ClassRequest request) {
        String email = getCurrentUserEmail();
        ClassEntity existingClass = checkClassExists(classId);
        validateClassLogic(request, true);

        Map<String, HistoryEntity.ChangeDetail> changes = new HashMap<>();
        changes.put("className", changed(existingClass.getClassName(), request.getClassName()));
        changes.put("status", changed(existingClass.getStatus(), request.getStatus()));
        changes.put("startDateClass", changed(existingClass.getStartDateClass(), request.getStartDateClass()));
        changes.put("endDateClass", changed(existingClass.getEndDateClass(), request.getEndDateClass()));

        String result = executeWithCatch(() -> {
            classMapper.updateEntityFromRequest(request, existingClass);
            existingClass.setUpdateDate(LocalDateTime.now());
            existingClass.setUpdateBy(email);
            return classRepository.update(classId, existingClass);
        }, "Lỗi hệ thống khi cập nhật lớp học");

        historyService.record(HistoryRequest.builder()
                .action(ActionType.UPDATE)
                .actorId(email)
                .actorName(email)
                .entityType(EntityType.CLASS.name())
                .entityId(classId)
                .entityName(existingClass.getClassName())
                .changes(changes)
                .build());

        return result;
    }

    @Override
    public String deleteClass(String classId) { // Bỏ deletedBy ở tham số nếu lấy từ Token
        String email = getCurrentUserEmail(); // Lấy từ SecurityContext
        ClassEntity existingClass = checkClassExists(classId);
        String result = executeWithCatch(() -> classRepository.softDeleteWithCascade(classId, email),
                "Lỗi hệ thống khi xóa lớp học");

        historyService.record(HistoryRequest.builder()
                .action(ActionType.DELETE)
                .actorId(email)
                .actorName(email)
                .entityType(EntityType.CLASS.name())
                .entityId(classId)
                .entityName(existingClass.getClassName())
                .changes(Map.of("deleted", changed(false, true)))
                .build());

        return result;
    }

    @Override
    public long countClasses() {
        return executeWithCatch(classRepository::countActive,
                "Lỗi hệ thống khi đếm số lượng lớp");
    }
}