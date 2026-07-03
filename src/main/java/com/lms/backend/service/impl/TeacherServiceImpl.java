package com.lms.backend.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.lms.backend.dto.request.TeacherRequest;
import com.lms.backend.dto.response.TeacherResponse;
import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.ProfileEntity;
import com.lms.backend.entity.RoleEntity;
import com.lms.backend.entity.TeacherEntity;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.TeacherMapper;
import com.lms.backend.repository.AccountRepository;
import com.lms.backend.repository.ProfileRepository;
import com.lms.backend.repository.RoleRepository;
import com.lms.backend.repository.TeacherRepository;
import com.lms.backend.service.ITeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements ITeacherService {

    private final Firestore firestore;

    private final TeacherRepository teacherRepository;
    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    private final TeacherMapper teacherMapper;

    private final PasswordEncoder passwordEncoder;

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

    private void validateTeacher(TeacherRequest request) {

        if (request.getDayOfBirth() != null && request.getDayOfBirth().isAfter(LocalDateTime.now())) {

            throw new BadRequestException("Ngày sinh không được lớn hơn thời điểm hiện tại.");
        }

        if (request.getAvailable() == null) {
            throw new BadRequestException("Trạng thái sẵn sàng không được để trống.");
        }
    }

    @Override
    public String createTeacher(TeacherRequest request) {

        validateTeacher(request);

        String email = getCurrentUserEmail();

        return executeWithCatch(() -> {

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

                return teacher.getId();

            }).get();

        }, "Lỗi hệ thống khi tạo giảng viên");
    }

    @Override
    public List<TeacherResponse> getAllTeachers() {

        return executeWithCatch(() -> {

            List<TeacherEntity> teachers = teacherRepository.findAllActive();

            return teachers.stream()

                    .filter(teacher -> teacher.getProfileId() != null)

                    .map(teacher -> {

                        ProfileEntity profile = checkProfileExists(teacher.getProfileId());

                        AccountEntity account = checkAccountExists(profile.getAccountId());

                        TeacherResponse response = teacherMapper.toResponse(account, profile, teacher);

//                        response.setClassNames(teacherRepository.getClassNamesByTeacherId(teacher.getId()));

                        return response;

                    }).toList();

        }, "Lỗi hệ thống khi lấy danh sách giảng viên");
    }

    @Override
    public TeacherResponse getTeacherDetail(String id) {

        return executeWithCatch(() -> {

            TeacherEntity teacher = checkTeacherExists(id);

            ProfileEntity profile = checkProfileExists(teacher.getProfileId());

            AccountEntity account = checkAccountExists(profile.getAccountId());

            TeacherResponse response = teacherMapper.toResponse(account, profile, teacher);

            response.setClassNames(teacherRepository.getClassNamesByTeacherId(teacher.getId()));

            return response;

        }, "Lỗi hệ thống khi lấy thông tin giảng viên");
    }

    @Override
    public String updateTeacher(String id, TeacherRequest request) {

        validateTeacher(request);

        String email = getCurrentUserEmail();

        return executeWithCatch(() -> {

            TeacherEntity teacher = checkTeacherExists(id);

            ProfileEntity profile = checkProfileExists(teacher.getProfileId());

            AccountEntity account = checkAccountExists(profile.getAccountId());

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

            return firestore.runTransaction(transaction -> {

                transaction.set(accountRepository.getAccountCollection().document(account.getId()), teacherMapper.accountToFirestoreMap(account), SetOptions.merge());

                transaction.set(profileRepository.getProfileCollection().document(profile.getId()), teacherMapper.profileToFirestoreMap(profile), SetOptions.merge());

                transaction.set(teacherRepository.getTeacherCollection().document(teacher.getId()), teacherMapper.teacherToFirestoreMap(teacher), SetOptions.merge());

                return "Cập nhật giảng viên thành công!";

            }).get();

        }, "Lỗi hệ thống khi cập nhật giảng viên");
    }

    @Override
    public String deleteTeacher(String id, String deletedBy) {

        return executeWithCatch(() -> {

            TeacherEntity teacher = checkTeacherExists(id);

            ProfileEntity profile = checkProfileExists(teacher.getProfileId());

            AccountEntity account = checkAccountExists(profile.getAccountId());

            account.setDeleted(true);
            profile.setDeleted(true);
            teacher.setDeleted(true);

            LocalDateTime now = LocalDateTime.now();

            account.setUpdateBy(deletedBy);
            account.setUpdateDate(now);

            profile.setUpdatedBy(deletedBy);
            profile.setUpdateDate(now);

            teacher.setUpdatedBy(deletedBy);
            teacher.setUpdateDate(now);

            return firestore.runTransaction(transaction -> {

                transaction.set(accountRepository.getAccountCollection().document(account.getId()), teacherMapper.accountToFirestoreMap(account), SetOptions.merge());

                transaction.set(profileRepository.getProfileCollection().document(profile.getId()), teacherMapper.profileToFirestoreMap(profile), SetOptions.merge());

                transaction.set(teacherRepository.getTeacherCollection().document(teacher.getId()), teacherMapper.teacherToFirestoreMap(teacher), SetOptions.merge());

                return "Xóa giảng viên thành công!";

            }).get();

        }, "Lỗi hệ thống khi xóa giảng viên");
    }
}