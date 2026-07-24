package com.lms.backend.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.lms.backend.dto.request.AttendanceRequest;
import com.lms.backend.dto.request.HistoryRequest;
import com.lms.backend.dto.request.TeacherClassRequest;
import com.lms.backend.dto.request.TeacherRequest;
import com.lms.backend.dto.response.*;
import com.lms.backend.entity.*;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.TeacherClassMapper;
import com.lms.backend.mapper.TeacherMapper;
import com.lms.backend.repository.*;
import com.lms.backend.service.IHistoryService;
import com.lms.backend.service.ITeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lms.backend.utils.HistoryChangeUtils.changed;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements ITeacherService {

    private final Firestore firestore;
    private final TeacherRepository teacherRepository;
    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final ClassRepository classRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final ClassSessionRepository classSessionRepository;
    private final StudentClassRepository studentClassRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    private final TeacherMapper teacherMapper;
    private final TeacherClassMapper teacherClassMapper;

    private final PasswordEncoder passwordEncoder;
    private final IHistoryService historyService;

    @Override
    public long countTeachers() throws Exception {
        return teacherRepository.countActive();
    }

    @Override
    public String createTeacher(TeacherRequest request) {
        validateCreateTeacher(request);
        String email = getCurrentUserEmail();
        request.setRequestedBy(email);

        String newId = executeWithCatch(() -> {
            AccountEntity account = teacherMapper.toAccountEntity(request);
            RoleEntity teacherRole = roleRepository.findByName("TEACHER");
            if (teacherRole == null) {
                throw new ResourceNotFoundException("Không tìm thấy role TEACHER.");
            }
            account.setRoleId(teacherRole.getId());
            account.setPassword(passwordEncoder.encode("123456"));

            ProfileEntity profile = teacherMapper.toProfileEntity(request);
            TeacherEntity teacher = teacherMapper.toTeacherEntity(request);

            account.setCreatedBy(email);
            account.setUpdateBy(email);
            profile.setCreatedBy(email);
            profile.setUpdatedBy(email);
            teacher.setCreatedBy(email);
            teacher.setUpdatedBy(email);

            return firestore.runTransaction(transaction -> {
                // Account
                DocumentReference accountRef = accountRepository.getAccountCollection().document();
                account.setId(accountRef.getId());
                transaction.set(accountRef, teacherMapper.accountToFirestoreMap(account));

                // Profile
                DocumentReference profileRef = profileRepository.getProfileCollection().document();
                profile.setId(profileRef.getId());
                profile.setAccountId(account.getId());
                transaction.set(profileRef, teacherMapper.profileToFirestoreMap(profile));

                // Teacher
                DocumentReference teacherRef = teacherRepository.getTeacherCollection().document();
                teacher.setId(teacherRef.getId());
                teacher.setProfileId(profile.getId());
                transaction.set(teacherRef, teacherMapper.teacherToFirestoreMap(teacher));

                // Teacher Classes
                if (request.getClasses() != null) {
                    for (TeacherClassRequest item : request.getClasses()) {
                        TeacherClassEntity tc = teacherClassMapper.toEntity(item.getClassId());
                        tc.setTeacherId(teacher.getId());
                        tc.setDeleted(false);
                        tc.setCreatedBy(email);
                        tc.setUpdatedBy(email);
                        tc.setCreateDate(LocalDateTime.now());
                        tc.setUpdateDate(LocalDateTime.now());

                        DocumentReference tcRef = teacherClassRepository.getTeacherClassCollection().document();
                        tc.setId(tcRef.getId());
                        transaction.set(tcRef, teacherClassMapper.toFirestoreMap(tc));
                    }
                }

                return teacher.getId();
            }).get();
        }, "Lỗi hệ thống khi tạo giảng viên");

        historyService.record(HistoryRequest.builder().action(ActionType.ADD).actorId(email).actorName(email).entityType(EntityType.TEACHER.name()).entityId(newId).entityName(request.getFullName()).changes(Map.of()).build());

        return newId;
    }

    @Override
    public String updateTeacher(String id, TeacherRequest request) {
        String email = getCurrentUserEmail();
        TeacherEntity teacher = checkTeacherExists(id);
        ProfileEntity profile = checkProfileExists(teacher.getProfileId());
        AccountEntity account = checkAccountExists(profile.getAccountId());

        validateUpdateTeacher(request, account, profile);

        Map<String, HistoryEntity.ChangeDetail> changes = buildUpdateChanges(teacher, profile, account, request);

        String result = executeWithCatch(() -> {
            teacherMapper.updateAccount(account, request);
            teacherMapper.updateProfile(profile, request);
            teacherMapper.updateTeacher(teacher, request);

            LocalDateTime now = LocalDateTime.now();
            account.setUpdateBy(email);
            account.setUpdateDate(now);
            profile.setUpdatedBy(email);
            profile.setUpdateDate(now);
            teacher.setUpdatedBy(email);
            teacher.setUpdateDate(now);

            firestore.runTransaction(transaction -> {
                transaction.set(accountRepository.getAccountCollection().document(account.getId()), teacherMapper.accountToFirestoreMap(account), SetOptions.merge());
                transaction.set(profileRepository.getProfileCollection().document(profile.getId()), teacherMapper.profileToFirestoreMap(profile), SetOptions.merge());
                transaction.set(teacherRepository.getTeacherCollection().document(teacher.getId()), teacherMapper.teacherToFirestoreMap(teacher), SetOptions.merge());
                return null;
            }).get();

            // Sync classes
            teacherClassRepository.syncTeacherClasses(teacher.getId(), request.getClasses(), email);

            return "Cập nhật giảng viên thành công!";
        }, "Lỗi hệ thống khi cập nhật giảng viên");

        historyService.record(HistoryRequest.builder().action(ActionType.UPDATE).actorId(email).actorName(email).entityType(EntityType.TEACHER.name()).entityId(teacher.getId()).entityName(profile.getFullName()).changes(changes).build());

        return result;
    }

    @Override
    public String deleteTeacher(String id, String deletedBy) throws Exception {
        return "";
    }

    @Override
    public String deleteTeacher(String id) {
        String email = getCurrentUserEmail();
        TeacherEntity teacher = checkTeacherExists(id);
        ProfileEntity profile = checkProfileExists(teacher.getProfileId());
        AccountEntity account = checkAccountExists(profile.getAccountId());

        String result = executeWithCatch(() -> {
            account.setDeleted(true);
            profile.setDeleted(true);
            teacher.setDeleted(true);

            LocalDateTime now = LocalDateTime.now();
            account.setUpdateBy(email);
            account.setUpdateDate(now);
            profile.setUpdatedBy(email);
            profile.setUpdateDate(now);
            teacher.setUpdatedBy(email);
            teacher.setUpdateDate(now);

            firestore.runTransaction(transaction -> {
                transaction.set(accountRepository.getAccountCollection().document(account.getId()), teacherMapper.accountToFirestoreMap(account), SetOptions.merge());
                transaction.set(profileRepository.getProfileCollection().document(profile.getId()), teacherMapper.profileToFirestoreMap(profile), SetOptions.merge());
                transaction.set(teacherRepository.getTeacherCollection().document(teacher.getId()), teacherMapper.teacherToFirestoreMap(teacher), SetOptions.merge());
                return null;
            }).get();

            teacherClassRepository.softDeleteByTeacherId(teacher.getId(), email);

            return "Xóa giảng viên thành công!";
        }, "Lỗi hệ thống khi xóa giảng viên");

        historyService.record(HistoryRequest.builder().action(ActionType.DELETE).actorId(email).actorName(email).entityType(EntityType.TEACHER.name()).entityId(teacher.getId()).entityName(profile.getFullName()).changes(Map.of("deleted", changed(false, true))).build());

        return result;
    }

    @Override
    public TeacherResponse getTeacherDetail(String id) {
        return executeWithCatch(() -> {
            TeacherEntity teacher = checkTeacherExists(id);
            ProfileEntity profile = checkProfileExists(teacher.getProfileId());
            AccountEntity account = checkAccountExists(profile.getAccountId());

            TeacherResponse response = teacherMapper.toResponse(account, profile, teacher);
            response.setClasses(teacherClassRepository.findClassesByTeacherId(teacher.getId()));

            return response;
        }, "Lỗi hệ thống khi lấy chi tiết giảng viên");
    }

    @Override
    public Map<String, Object> getAllTeachers(int page, int size, String keyword, String classId, Boolean available) {
        return executeWithCatch(() -> {
            // Load all data once
            List<TeacherEntity> allTeachers = teacherRepository.findAllActive();
            List<ProfileEntity> profiles = profileRepository.findAllActive();
            List<AccountEntity> accounts = accountRepository.findAllActiveEntities();
            List<TeacherClassEntity> teacherClasses = teacherClassRepository.findAllActive();
            List<ClassEntity> classes = classRepository.findAllActive();

            // Lookup maps
            Map<String, ProfileEntity> profileMap = profiles.stream().collect(Collectors.toMap(ProfileEntity::getId, Function.identity()));
            Map<String, AccountEntity> accountMap = accounts.stream().collect(Collectors.toMap(AccountEntity::getId, Function.identity()));
            Map<String, ClassEntity> classMap = classes.stream().collect(Collectors.toMap(ClassEntity::getId, Function.identity()));
            Map<String, List<TeacherClassEntity>> teacherClassMap = teacherClasses.stream().collect(Collectors.groupingBy(TeacherClassEntity::getTeacherId));

            String search = keyword == null ? "" : keyword.trim().toLowerCase();

            List<TeacherEntity> filteredList = allTeachers.stream().filter(t -> t.getProfileId() != null).filter(t -> search.isBlank() || matchesSearch(t, profileMap, accountMap, search)).filter(t -> available == null || available.equals(t.getAvailable())).filter(t -> classId == null || classId.isBlank() || matchesClass(t, teacherClassMap, classId)).sorted((t1, t2) -> t2.getCreateDate().compareTo(t1.getCreateDate())).toList();

            // Phân trang
            int totalElements = filteredList.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / size));
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalElements);

            List<TeacherListResponse> items = (fromIndex < totalElements) ? buildTeacherListResponses(filteredList.subList(fromIndex, toIndex), profileMap, accountMap, teacherClassMap, classMap) : List.of();

            Map<String, Object> response = new HashMap<>();
            response.put("items", items);
            response.put("totalPages", totalPages);
            response.put("totalElements", totalElements);

            return response;
        }, "Lỗi hệ thống khi lấy danh sách giảng viên");
    }

    @Override
    public List<TeacherResponse> getAllTeachers() throws Exception {
        return List.of();
    }

    @Override
    public String getTeacherIdByAccountId(String accountId) {
        return executeWithCatch(() -> {
            ProfileEntity profile = profileRepository.findByAccountId(accountId);
            if (profile == null || Boolean.TRUE.equals(profile.getDeleted())) {
                throw new ResourceNotFoundException("Không tìm thấy hồ sơ cho tài khoản này.");
            }

            TeacherEntity teacher = teacherRepository.findByProfileId(profile.getId());
            if (teacher == null || Boolean.TRUE.equals(teacher.getDeleted())) {
                throw new ResourceNotFoundException("Tài khoản này không phải là giảng viên.");
            }

            return teacher.getId();
        }, "Lỗi hệ thống khi lấy Teacher ID từ Account ID");
    }

    @Override
    public Map<String, Object> getClassesByTeacher(String teacherId, int page, int size, String name, String program, String status, String startDate, String endDate) {
        return executeWithCatch(() -> {
            List<TeacherClassEntity> teacherClasses = teacherClassRepository.findByTeacherId(teacherId);
            if (teacherClasses == null || teacherClasses.isEmpty()) {
                return Map.of("items", List.of(), "totalPages", 0, "totalElements", 0);
            }

            List<String> classIds = teacherClasses.stream().filter(tc -> !Boolean.TRUE.equals(tc.getDeleted())).map(TeacherClassEntity::getClassId).toList();

            if (classIds.isEmpty()) {
                return Map.of("items", List.of(), "totalPages", 0, "totalElements", 0);
            }

            List<ClassEntity> allClasses = classRepository.findAllByIds(classIds);

            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // FE gửi lên định dạng yyyy-MM-dd
            DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDateTime startFilter = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate, parseFormatter).atStartOfDay() : null;

            LocalDateTime endFilter = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate, parseFormatter).atTime(23, 59, 59) : null;

            List<TeacherClassResponse> classResponses = allClasses.stream()
                    // Lọc theo trạng thái: nếu FE không truyền -> mặc định chỉ ACTIVE (giữ hành vi cũ)
                    .filter(c -> (status != null && !status.isEmpty()) ? status.equalsIgnoreCase(c.getStatus()) : "ACTIVE".equalsIgnoreCase(c.getStatus()))
                    // Lọc theo tên (chứa từ khóa, không phân biệt hoa thường)
                    .filter(c -> {
                        if (name != null && !name.isEmpty()) {
                            return c.getClassName() != null && c.getClassName().toLowerCase().contains(name.toLowerCase());
                        }
                        return true;
                    })
                    // Lọc theo chương trình học
                    .filter(c -> {
                        if (program != null && !program.isEmpty()) {
                            return program.equals(c.getPrograme());
                        }
                        return true;
                    })
                    // Lọc theo ngày bắt đầu (lớp bắt đầu SAU hoặc BẰNG ngày filter)
                    .filter(c -> {
                        if (startFilter != null && c.getStartDateClass() != null) {
                            return !c.getStartDateClass().isBefore(startFilter);
                        }
                        return true;
                    })
                    // Lọc theo ngày kết thúc (lớp kết thúc TRƯỚC hoặc BẰNG ngày filter)
                    .filter(c -> {
                        if (endFilter != null && c.getEndDateClass() != null) {
                            return !c.getEndDateClass().isAfter(endFilter);
                        }
                        return true;
                    }).map(c -> TeacherClassResponse.builder().classId(c.getId()).className(c.getClassName()).program(c.getPrograme()).startDate(c.getStartDateClass() != null ? c.getStartDateClass().format(displayFormatter) : "").endDate(c.getEndDateClass() != null ? c.getEndDateClass().format(displayFormatter) : "").status(c.getStatus() != null ? c.getStatus() : "UNKNOWN").build()).toList();

            int totalElements = classResponses.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / size));
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalElements);

            List<TeacherClassResponse> paginatedClasses = (fromIndex < totalElements) ? classResponses.subList(fromIndex, toIndex) : List.of();

            return Map.of("items", paginatedClasses, "totalPages", totalPages, "totalElements", totalElements);
        }, "Lỗi hệ thống khi lấy danh sách lớp học của giảng viên");
    }

    @Override
    public Map<String, Object> getClassSessions(String classId, int page, int size) {
        return executeWithCatch(() -> {
            List<ClassSessionEntity> allSessions = classSessionRepository.findByClassId(classId);

            DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Sắp xếp theo ngày tăng dần (buổi 1 -> buổi cuối)
            List<ClassSessionEntity> sorted = allSessions.stream().sorted((s1, s2) -> {
                try {
                    return LocalDate.parse(s1.getSessionDate(), parseFormatter).compareTo(LocalDate.parse(s2.getSessionDate(), parseFormatter));
                } catch (Exception e) {
                    return 0;
                }
            }).toList();

            int totalElements = sorted.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / size));
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalElements);

            List<ClassSessionResponse> items = (fromIndex < totalElements) ? sorted.subList(fromIndex, toIndex).stream().map(s -> ClassSessionResponse.builder().id(s.getId()).title(s.getTitle()).sessionDate(formatSessionDate(s.getSessionDate(), parseFormatter, displayFormatter)).startTime(s.getStartTime()).endTime(s.getEndTime()).status(s.getStatus()).build()).toList() : List.of();

            return Map.of("items", items, "totalPages", totalPages, "totalElements", totalElements);
        }, "Lỗi hệ thống khi lấy lịch trình buổi học của lớp");
    }

    private String formatSessionDate(String raw, DateTimeFormatter parseFormatter, DateTimeFormatter displayFormatter) {
        try {
            return LocalDate.parse(raw, parseFormatter).format(displayFormatter);
        } catch (Exception e) {
            return raw;
        }
    }

    @Override
    public Map<String, Object> getSessionAttendance(String classId, String sessionId) {
        return executeWithCatch(() -> {
            List<StudentClassEntity> studentClasses = studentClassRepository.findByClassId(classId);
            List<AttendanceEntity> attendanceRecords = attendanceRepository.findBySession(classId, sessionId);

            Map<String, AttendanceEntity> attendanceMap = attendanceRecords.stream().collect(Collectors.toMap(AttendanceEntity::getStudentId, a -> a));

            List<AttendanceStudentResponse> items = new ArrayList<>();

            for (StudentClassEntity sc : studentClasses) {
                StudentEntity student = studentRepository.findById(sc.getStudentId());
                if (student == null || Boolean.TRUE.equals(student.getDeleted())) continue;

                ProfileEntity profile = profileRepository.findById(student.getProfileId());
                if (profile == null) continue;

                AccountEntity account = accountRepository.findEntityById(profile.getAccountId());

                AttendanceEntity existing = attendanceMap.get(sc.getStudentId());

                items.add(AttendanceStudentResponse.builder().studentId(sc.getStudentId()).fullName(profile.getFullName()).phoneNumber(profile.getPhoneNumber()).email(account != null ? account.getEmail() : "").status(existing != null ? existing.getStatus() : "Chưa điểm danh").note(existing != null ? existing.getNote() : "").build());
            }

            return Map.of("items", items, "totalElements", items.size());
        }, "Lỗi hệ thống khi lấy danh sách điểm danh");
    }

    @Override
    public String saveSessionAttendance(String classId, String sessionId, List<AttendanceRequest> records) {
        String email = getCurrentUserEmail();
        return executeWithCatch(() -> {
            List<AttendanceEntity> entities = records.stream().map(r -> AttendanceEntity.builder().studentId(r.getStudentId()).status(r.getStatus()).note(r.getNote()).build()).toList();

            attendanceRepository.saveAll(classId, sessionId, entities, email);

            return "Lưu điểm danh thành công!";
        }, "Lỗi hệ thống khi lưu điểm danh");
    }

    @Override
    public List<TeacherScheduleResponse> getTeacherSchedules(String teacherId) {
        return executeWithCatch(() -> {
            List<TeacherClassEntity> teacherClasses = teacherClassRepository.findByTeacherId(teacherId);
            if (teacherClasses == null || teacherClasses.isEmpty()) {
                return List.of();
            }

            List<String> classIds = teacherClasses.stream().map(TeacherClassEntity::getClassId).toList();

            List<ClassEntity> allClasses = classRepository.findAllByIds(classIds);
            List<ClassEntity> activeClasses = allClasses.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).toList();

            List<TeacherScheduleResponse> schedules = new ArrayList<>();

            for (ClassEntity clazz : activeClasses) {
                String displaySessionDate = formatScheduleDays(clazz.getScheduleDays());

                schedules.add(TeacherScheduleResponse.builder().id(clazz.getId()).classId(clazz.getId()).className(clazz.getClassName()).title(clazz.getDescription()).sessionDate(displaySessionDate).startTime(clazz.getStartTime()).endTime(clazz.getEndTime()).location("Văn phòng CodeGym, Đà Nẵng").build());
            }
            return schedules;
        }, "Lỗi hệ thống khi lấy lịch dạy");
    }

    @Override
    public TeacherStatsResponse getTeacherStats(String teacherId) {
        return executeWithCatch(() -> {
            List<TeacherClassEntity> teacherClasses = teacherClassRepository.findByTeacherId(teacherId);
            if (teacherClasses == null || teacherClasses.isEmpty()) {
                return TeacherStatsResponse.builder().totalClasses(0).totalStudents(0).build();
            }

            List<String> classIds = teacherClasses.stream().map(TeacherClassEntity::getClassId).toList();

            List<ClassEntity> allClasses = classRepository.findAllByIds(classIds);

            long activeClassesCount = allClasses.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();

            int totalStudents = 0;

            return TeacherStatsResponse.builder().totalClasses((int) activeClassesCount).totalStudents(totalStudents).build();
        }, "Lỗi hệ thống khi lấy thống kê giảng viên");
    }

    private String formatScheduleDays(List<Integer> scheduleDays) {
        if (scheduleDays == null || scheduleDays.isEmpty()) {
            return "Chưa có lịch";
        }

        Map<Integer, String> dayMap = Map.of(0, "Chủ Nhật", 1, "Thứ 2", 2, "Thứ 3", 3, "Thứ 4", 4, "Thứ 5", 5, "Thứ 6", 6, "Thứ 7", 7, "Chủ Nhật");

        List<String> formattedDays = new ArrayList<>();

        for (Integer day : scheduleDays) {
            if (dayMap.containsKey(day)) {
                formattedDays.add(dayMap.get(day));
            }
        }

        return String.join(", ", formattedDays);
    }

    private <T> T executeWithCatch(Callable<T> task, String errorMessage) {
        try {
            return task.call();
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(errorMessage + ": " + e.getMessage(), e);
        }
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
        if (principal instanceof String principalName && !"anonymousUser".equals(principalName)) {
            return principalName;
        }
        throw new IllegalStateException("Authentication required");
    }

    private TeacherEntity checkTeacherExists(String id) {
        return executeWithCatch(() -> {
            TeacherEntity teacher = teacherRepository.findById(id);
            if (teacher == null || Boolean.TRUE.equals(teacher.getDeleted())) {
                throw new ResourceNotFoundException("Không tìm thấy giảng viên.");
            }
            return teacher;
        }, "Lỗi hệ thống khi lấy thông tin giảng viên");
    }

    private ProfileEntity checkProfileExists(String profileId) {
        return executeWithCatch(() -> {
            ProfileEntity profile = profileRepository.findById(profileId);
            if (profile == null || Boolean.TRUE.equals(profile.getDeleted())) {
                throw new ResourceNotFoundException("Không tìm thấy hồ sơ.");
            }
            return profile;
        }, "Lỗi hệ thống khi lấy thông tin hồ sơ");
    }

    private AccountEntity checkAccountExists(String accountId) {
        return executeWithCatch(() -> {
            AccountEntity account = accountRepository.findEntityById(accountId);
            if (account == null || Boolean.TRUE.equals(account.getDeleted())) {
                throw new ResourceNotFoundException("Không tìm thấy tài khoản.");
            }
            return account;
        }, "Lỗi hệ thống khi lấy thông tin tài khoản");
    }

    private void validateCreateTeacher(TeacherRequest request) {
        try {
            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email đã tồn tại.");
            }
            if (profileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BadRequestException("Số điện thoại đã tồn tại.");
            }
        } catch (Exception e) {
            if (e instanceof BadRequestException) throw (BadRequestException) e;
            throw new RuntimeException(e);
        }

        if (request.getDayOfBirth() != null && request.getDayOfBirth().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Ngày sinh không được lớn hơn thời điểm hiện tại.");
        }
    }

    private void validateUpdateTeacher(TeacherRequest request, AccountEntity account, ProfileEntity profile) {
        try {
            if (accountRepository.existsByEmailExceptId(request.getEmail(), account.getId())) {
                throw new BadRequestException("Email đã tồn tại.");
            }
            if (profileRepository.existsByPhoneNumberExceptId(request.getPhoneNumber(), profile.getId())) {
                throw new BadRequestException("Số điện thoại đã tồn tại.");
            }
        } catch (Exception e) {
            if (e instanceof BadRequestException) throw (BadRequestException) e;
            throw new RuntimeException(e);
        }
    }

    private boolean matchesSearch(TeacherEntity teacher, Map<String, ProfileEntity> profileMap, Map<String, AccountEntity> accountMap, String search) {
        ProfileEntity profile = profileMap.get(teacher.getProfileId());
        if (profile == null) return false;

        AccountEntity account = accountMap.get(profile.getAccountId());
        if (account == null) return false;

        return account.getEmail().toLowerCase().contains(search) || profile.getFullName().toLowerCase().contains(search) || (profile.getPhoneNumber() != null && profile.getPhoneNumber().contains(search));
    }

    private boolean matchesClass(TeacherEntity teacher, Map<String, List<TeacherClassEntity>> teacherClassMap, String classId) {
        return teacherClassMap.getOrDefault(teacher.getId(), List.of()).stream().anyMatch(tc -> classId.equals(tc.getClassId()));
    }

    private List<TeacherListResponse> buildTeacherListResponses(List<TeacherEntity> teachers, Map<String, ProfileEntity> profileMap, Map<String, AccountEntity> accountMap, Map<String, List<TeacherClassEntity>> teacherClassMap, Map<String, ClassEntity> classMap) {

        return teachers.stream().map(teacher -> {
            ProfileEntity profile = profileMap.get(teacher.getProfileId());
            if (profile == null) return null;

            AccountEntity account = accountMap.get(profile.getAccountId());
            if (account == null) return null;

            TeacherListResponse response = TeacherListResponse.builder().id(teacher.getId()).fullName(profile.getFullName()).email(account.getEmail()).phoneNumber(profile.getPhoneNumber()).gender(profile.getGender()).available(teacher.getAvailable()).build();

            List<TeacherClassResponse> classResponses = teacherClassMap.getOrDefault(teacher.getId(), List.of()).stream().map(tc -> {
                ClassEntity clazz = classMap.get(tc.getClassId());
                // trong method buildTeacherListResponses(...), trước khi map/create list:
                DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                // rồi trong map:
                return TeacherClassResponse.builder().classId(tc.getClassId()).className(clazz != null ? clazz.getClassName() : "").program(clazz != null ? clazz.getPrograme() : "").startDate(clazz != null && clazz.getStartDateClass() != null ? clazz.getStartDateClass().format(displayFormatter) : "").endDate(clazz != null && clazz.getEndDateClass() != null ? clazz.getEndDateClass().format(displayFormatter) : "").status(clazz != null && clazz.getStatus() != null ? clazz.getStatus() : "UNKNOWN").build();
            }).toList();

            response.setClasses(classResponses);
            return response;
        }).filter(Objects::nonNull).toList();
    }

    private Map<String, HistoryEntity.ChangeDetail> buildUpdateChanges(TeacherEntity teacher, ProfileEntity profile, AccountEntity account, TeacherRequest request) {
        Map<String, HistoryEntity.ChangeDetail> changes = new HashMap<>();
        changes.put("full_name", changed(profile.getFullName(), request.getFullName()));
        changes.put("email", changed(account.getEmail(), request.getEmail()));
        changes.put("phone_number", changed(profile.getPhoneNumber(), request.getPhoneNumber()));
        changes.put("gender", changed(profile.getGender(), request.getGender()));
        changes.put("address", changed(profile.getAddress(), request.getAddress()));
        changes.put("specialization", changed(teacher.getSpecialization(), request.getSpecialization()));
        changes.put("qualification", changed(teacher.getQualification(), request.getQualification()));
        changes.put("certifications", changed(teacher.getCertifications(), request.getCertifications()));
        changes.put("available", changed(teacher.getAvailable(), request.getAvailable()));
        return changes;
    }
}