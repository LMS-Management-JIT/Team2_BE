package com.lms.backend.service.impl;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
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
import java.util.ArrayList;
import java.util.HashMap;
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
    public Map<String, Object> getAllClasses(int page, int size, String name, String program, String status, String startDate, String endDate) {
        return executeWithCatch(() -> {
            // 1. Khởi tạo Query gốc trên Firestore (CHỈ LỌC CÁC ĐIỀU KIỆN BẰNG NHAU)
            CollectionReference classesRef = classRepository.getClassCollection();
            Query query = classesRef.whereEqualTo("deleted", false);

            if (status != null && !status.isEmpty()) {
                query = query.whereEqualTo("status", status);
            }
            if (program != null && !program.isEmpty()) {
                query = query.whereEqualTo("programe", program);
            }

            // 2. Lấy toàn bộ dữ liệu thỏa mãn điều kiện 'bằng' về Java
            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

            // Map sang Entity
            List<ClassEntity> allEntities = documents.stream()
                    .map(classMapper::fromDocument)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            // 3. Xử lý Date formatter (FE đang gửi lên định dạng yyyy-MM-dd)
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDateTime startFilter = (startDate != null && !startDate.isEmpty())
                    ? java.time.LocalDate.parse(startDate, formatter).atStartOfDay()
                    : null;

            LocalDateTime endFilter = (endDate != null && !endDate.isEmpty())
                    ? java.time.LocalDate.parse(endDate, formatter).atTime(23, 59, 59)
                    : null;

            // 4. Lọc Tên và Ngày tháng bằng Java Stream
            List<ClassEntity> filteredList = allEntities.stream()
                    .filter(c -> {
                        // Lọc Tên (Chứa từ khóa, không phân biệt hoa thường)
                        if (name != null && !name.isEmpty()) {
                            if (c.getClassName() == null || !c.getClassName().toLowerCase().contains(name.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .filter(c -> {
                        // Lọc Ngày bắt đầu (Lớp học phải bắt đầu SAU hoặc BẰNG ngày filter)
                        if (startFilter != null && c.getStartDateClass() != null) {
                            if (c.getStartDateClass().isBefore(startFilter)) return false;
                        }
                        return true;
                    })
                    .filter(c -> {
                        // Lọc Ngày kết thúc (Lớp học phải kết thúc TRƯỚC hoặc BẰNG ngày filter)
                        if (endFilter != null && c.getEndDateClass() != null) {
                            if (c.getEndDateClass().isAfter(endFilter)) return false;
                        }
                        return true;
                    })
                    // Thêm sắp xếp nếu muốn (VD: mới nhất lên đầu)
                    .sorted((c1, c2) -> c2.getCreateDate().compareTo(c1.getCreateDate()))
                    .collect(Collectors.toList());

            // 5. Phân trang thủ công trên list đã lọc
            int totalElements = filteredList.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalElements);

            List<ClassResponse> items = new ArrayList<>();
            if (fromIndex < totalElements) {
                items = filteredList.subList(fromIndex, toIndex).stream()
                        .map(classMapper::toResponse)
                        .collect(Collectors.toList());
            }

            // 6. Đóng gói kết quả
            Map<String, Object> response = new HashMap<>();
            response.put("items", items);
            response.put("totalPages", totalPages > 0 ? totalPages : 1);

            return response;
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
        return executeWithCatch(() -> {
            classMapper.updateEntityFromRequest(request, existingClass);
            existingClass.setUpdateDate(LocalDateTime.now());
            existingClass.setUpdateBy(email);
            return classRepository.update(classId, existingClass);
        }, "Lỗi hệ thống khi cập nhật lớp học");
    }

    @Override
    public String deleteClass(String classId) { // Bỏ deletedBy ở tham số nếu lấy từ Token
        String email = getCurrentUserEmail(); // Lấy từ SecurityContext
        checkClassExists(classId);
        return executeWithCatch(() -> classRepository.softDeleteWithCascade(classId, email),
                "Lỗi hệ thống khi xóa lớp học");
    }

    @Override
    public long countClasses() {
        return executeWithCatch(classRepository::countActive,
                "Lỗi hệ thống khi đếm số lượng lớp");
    }
}