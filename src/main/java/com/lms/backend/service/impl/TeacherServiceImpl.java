package com.lms.backend.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.lms.backend.dto.request.HistoryRequest;
import com.lms.backend.dto.request.TeacherClassRequest;
import com.lms.backend.dto.request.TeacherRequest;
import com.lms.backend.dto.response.TeacherClassResponse;
import com.lms.backend.dto.response.TeacherListResponse;
import com.lms.backend.dto.response.TeacherResponse;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private final TeacherMapper teacherMapper;
    private final TeacherClassMapper teacherClassMapper;          // ← cần
    private final TeacherClassRepository teacherClassRepository; // ← quan trọng

    private final PasswordEncoder passwordEncoder;
    private final IHistoryService historyService;
    private final ClassRepository classRepository;

    @Override
    public long countTeachers() throws Exception {
        return teacherRepository.countActive();
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
            List<TeacherClassEntity> teacherClasses = teacherClassRepository.findAllActive(); // bạn cần thêm method này
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
                return TeacherClassResponse.builder().classId(tc.getClassId()).className(clazz != null ? clazz.getClassName() : "").build();
            }).toList();

            response.setClasses(classResponses);
            return response;
        }).filter(Objects::nonNull).toList();
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
}