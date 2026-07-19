package com.lms.backend.service.impl;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
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
import com.lms.backend.service.ICourseService;
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
    private final ICourseService courseService;

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

    private void validateClassLogic(ClassRequest request, boolean isUpdate, String classId) {
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
        String existingClassId = classRepository.findByClassName(request.getClassName());

        if (isUpdate) {
            // Khi cập nhật: Tìm thấy tên này VÀ ID tìm được KHÁC với ID của lớp đang sửa -> Trùng!
            if (existingClassId != null && !existingClassId.equals(classId)) {
                throw new BadRequestException("Tên lớp '" + request.getClassName() + "' đã tồn tại trong hệ thống!");
            }
        } else {
            // Khi tạo mới: Cứ tìm thấy ID nào dùng tên này là báo Trùng!
            if (existingClassId != null) {
                throw new BadRequestException("Tên lớp '" + request.getClassName() + "' đã tồn tại trong hệ thống!");
            }
        }
    }

    @Override
    public String createClass(ClassRequest request) {
        String email = getCurrentUserEmail();
        validateClassLogic(request, false, null);

        String newId = executeWithCatch(() -> {
            // 1. Gọi Repo lưu thông tin lớp học trước để lấy classId
            ClassEntity entity = classMapper.toEntity(request);
            entity.setStartDateClass(request.getStartDateClass());
            entity.setCreateDate(LocalDateTime.now());
            entity.setCreatedBy(email);
            entity.setDeleted(false);
            String classId = classRepository.save(entity);

            // 2. Xử lý logic tính toán lịch trình (Thuần Java logic)
            if (request.getScheduleDays() != null && !request.getScheduleDays().isEmpty()) {
                List<Map<String, Object>> sessionsDataList = new ArrayList<>();

                java.time.LocalDate currentDate = request.getStartDateClass().toLocalDate();
                java.time.LocalDate endDate = request.getEndDateClass().toLocalDate();
                int sessionCount = 1;

                while (!currentDate.isAfter(endDate)) {
                    int currentDayOfWeek = currentDate.getDayOfWeek().getValue(); // 1 = Thứ 2, ..., 7 = Chủ Nhật

                    if (request.getScheduleDays().contains(currentDayOfWeek)) {
                        Map<String, Object> session = new HashMap<>();
                        session.put("class_id", classId);
                        session.put("session_date", currentDate.toString());
                        session.put("start_time", request.getStartTime());
                        session.put("end_time", request.getEndTime());
                        session.put("title", "Buổi " + sessionCount);
                        session.put("status", "Chưa điểm danh");
                        session.put("deleted", false);

                        sessionsDataList.add(session);
                        sessionCount++;
                    }
                    currentDate = currentDate.plusDays(1);
                }

                if (!sessionsDataList.isEmpty()) {
                    classRepository.saveClassSessionsBatch(classId, sessionsDataList);
                }
            }

            return classId;
        }, "Lỗi hệ thống khi tạo lớp học và sinh lịch học");

        // Ghi nhận History (Giữ nguyên)
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

    // Hàm lấy lịch trình lớp học gọi từ Impl
    @Override
    public List<Map<String, Object>> getClassSessions(String classId) {
        checkClassExists(classId); // Đảm bảo lớp học tồn tại
        return executeWithCatch(() -> {
            List<QueryDocumentSnapshot> docs = classRepository.findSessionsByClassId(classId);
            List<Map<String, Object>> sessions = new ArrayList<>();

            for (QueryDocumentSnapshot doc : docs) {
                sessions.add(doc.getData());
            }
            return sessions;
        }, "Lỗi hệ thống khi lấy lịch trình lớp học");
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
                    .sorted((c1, c2) -> {
                        int dateCompare = c2.getCreateDate().compareTo(c1.getCreateDate());
                        if (dateCompare == 0) {
                            if (c1.getId() != null && c2.getId() != null) {
                                return c1.getId().compareTo(c2.getId());
                            }
                        }
                        return dateCompare;
                    })
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
            detailResponse.setCourse(courseService.getCourseById(classEntity.getCourseId()));
            detailResponse.setStudents(this.getStudentsOfClass(classId));
            detailResponse.setTeachers(this.getTeachersOfClass(classId));
            return detailResponse;
        }, "Lỗi hệ thống khi lấy chi tiết liên kết của lớp học");
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
        validateClassLogic(request, true, classId);
        Map<String, HistoryEntity.ChangeDetail> changes = new HashMap<>();
        changes.put("class_name", changed(existingClass.getClassName(), request.getClassName()));
        changes.put("status", changed(existingClass.getStatus(), request.getStatus()));
        changes.put("start_date_class", changed(existingClass.getStartDateClass(), request.getStartDateClass()));
        changes.put("end_date_class", changed(existingClass.getEndDateClass(), request.getEndDateClass()));
        changes.put("description", changed(existingClass.getDescription(), request.getDescription()));
        changes.put("note", changed(existingClass.getNote(), request.getNote()));
        changes.put("programe", changed(existingClass.getPrograme(), request.getPrograme()));
        changes.put("course_id", changed(existingClass.getCourseId(), request.getCourseId()));

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