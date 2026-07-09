package com.lms.backend.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.lms.backend.dto.request.StudentClassRequest;
import com.lms.backend.dto.request.StudentRequest;
import com.lms.backend.dto.response.StudentResponse;
import com.lms.backend.entity.*;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.ClassMapper;
import com.lms.backend.mapper.StudentClassMapper;
import com.lms.backend.mapper.StudentMapper;
import com.lms.backend.repository.*;
import com.lms.backend.service.IStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements IStudentService {

    private final Firestore firestore;
    private final StudentRepository studentRepository;
    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final StudentClassRepository studentClassRepository;
    private final StudentClassMapper studentClassMapper;


    private final ClassRepository classRepository;

    private final ClassMapper classMapper;


    @Override
    public long countStudents() throws Exception {
        return studentRepository.countActive();
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

    private StudentEntity checkStudentExists(String id) {
        return executeWithCatch(() -> {
            StudentEntity student = studentRepository.findById(id);
            if (student == null || Boolean.TRUE.equals(student.getDeleted())) {
                throw new ResourceNotFoundException(
                        "Không tìm thấy học viên.");
            }
            return student;
        }, "Lỗi hệ thống khi lấy thông tin học viên");
    }

    private ProfileEntity checkProfileExists(String profileId) {
        return executeWithCatch(() -> {
            ProfileEntity profile =
                    profileRepository.findById(profileId);
            if (profile == null || Boolean.TRUE.equals(profile.getDeleted())) {
                throw new ResourceNotFoundException(
                        "Không tìm thấy hồ sơ.");
            }
            return profile;
        }, "Lỗi hệ thống khi lấy thông tin hồ sơ");
    }

    private AccountEntity checkAccountExists(String accountId) {
        return executeWithCatch(() -> {
            AccountEntity account =
                    accountRepository.findEntityById(accountId);
            if (account == null || Boolean.TRUE.equals(account.getDeleted())) {
                throw new ResourceNotFoundException(
                        "Không tìm thấy tài khoản.");
            }
            return account;
        }, "Lỗi hệ thống khi lấy thông tin tài khoản");
    }

    private String getCurrentUserEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
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

    private void validateStudent(StudentRequest request) {
        if (request.getIssueDate() != null &&
                request.getIssueDate().isAfter(LocalDateTime.now())) {
            throw new BadRequestException(
                    "Ngày cấp CCCD không được lớn hơn thời điểm hiện tại.");
        }
        if (request.getDayOfBirth() != null &&
                request.getIssueDate() != null &&
                request.getIssueDate().isBefore(request.getDayOfBirth())) {
            throw new BadRequestException(
                    "Ngày cấp CCCD phải sau ngày sinh.");
        }
    }



    @Override
    public String createStudent(StudentRequest request) {
        validateStudent(request);
        String email = getCurrentUserEmail();

        return executeWithCatch(() -> {

            AccountEntity account =
                    studentMapper.toAccountEntity(request);

            RoleEntity studentRole = roleRepository.findByName("STUDENT");
            account.setRoleId(studentRole.getId());

            ProfileEntity profile =
                    studentMapper.toProfileEntity(request);

            StudentEntity student =
                    studentMapper.toStudentEntity(request);

            student.setStatus(request.getStatus());

            account.setPassword(passwordEncoder.encode("123456"));

            account.setCreatedBy(email);
            account.setUpdateBy(email);

            profile.setCreatedBy(email);
            profile.setUpdatedBy(email);

            student.setCreatedBy(email);
            student.setUpdatedBy(email);

            return firestore.runTransaction(transaction -> {

                // ==========================
                // Account
                // ==========================
                DocumentReference accountRef =
                        accountRepository.getAccountCollection().document();

                account.setId(accountRef.getId());

                transaction.set(
                        accountRef,
                        studentMapper.accountToFirestoreMap(account)
                );

                // ==========================
                // Profile
                // ==========================
                DocumentReference profileRef =
                        profileRepository.getProfileCollection().document();

                profile.setId(profileRef.getId());
                profile.setAccountId(account.getId());

                transaction.set(
                        profileRef,
                        studentMapper.profileToFirestoreMap(profile)
                );

                // ==========================
                // Student
                // ==========================
                DocumentReference studentRef =
                        studentRepository.getStudentCollection().document();

                student.setId(studentRef.getId());
                student.setProfileId(profile.getId());

                transaction.set(
                        studentRef,
                        studentMapper.studentToFirestoreMap(student)
                );

                // ==========================
                // Student Class (THÊM MỚI)
                // ==========================
                if (request.getClasses() != null) {

                    for (StudentClassRequest item : request.getClasses()) {

                        StudentClassEntity studentClass =
                                studentClassMapper.toEntity(item.getClassId());

                        studentClass.setStudentId(student.getId());

                        studentClass.setStatus(item.getStatus());
                        studentClass.setDeleted(false);

                        studentClass.setCreatedBy(email);
                        studentClass.setUpdatedBy(email);

                        studentClass.setCreateDate(LocalDateTime.now());
                        studentClass.setUpdateDate(LocalDateTime.now());

                        DocumentReference studentClassRef =
                                studentClassRepository
                                        .getStudentClassCollection()
                                        .document();

                        studentClass.setId(studentClassRef.getId());

                        transaction.set(
                                studentClassRef,
                                studentClassMapper.toFirestoreMap(studentClass)
                        );
                    }
                }

                return student.getId();

            }).get();

        }, "Lỗi hệ thống khi tạo học viên");
    }

    @Override
    public Map<String, Object> getAllStudents(int page, int size) {

        return executeWithCatch(() -> {

            // 1. Lấy toàn bộ học viên đang hoạt động
            List<StudentEntity> allStudents =
                    studentRepository.findAllActive();

            // 2. (Có thể thêm filter ở đây sau này)
            List<StudentEntity> filteredList = allStudents.stream()
                    .filter(student -> student.getProfileId() != null)
                    .sorted((s1, s2) ->
                            s2.getCreateDate().compareTo(s1.getCreateDate()))
                    .toList();

            // 3. Phân trang
            int totalElements = filteredList.size();

            int totalPages =
                    (int) Math.ceil((double) totalElements / size);

            int fromIndex = (page - 1) * size;

            int toIndex = Math.min(fromIndex + size, totalElements);

            List<StudentResponse> items = new ArrayList<>();

            if (fromIndex < totalElements) {

                items = filteredList
                        .subList(fromIndex, toIndex)
                        .stream()
                        .map(student -> {

                            ProfileEntity profile =
                                    checkProfileExists(student.getProfileId());

                            AccountEntity account =
                                    checkAccountExists(profile.getAccountId());

                            StudentResponse response =
                                    studentMapper.toResponse(
                                            account,
                                            profile,
                                            student
                                    );

                            response.setClasses(
                                    studentClassRepository.findClassesByStudentId(
                                            student.getId()
                                    )
                            );

                            return response;

                        })
                        .toList();
            }

            // 4. Trả kết quả
            Map<String, Object> response = new HashMap<>();

            response.put("items", items);
            response.put("totalPages", totalPages > 0 ? totalPages : 1);

            return response;

        }, "Lỗi hệ thống khi lấy danh sách học viên");
    }

    @Override
    public StudentResponse getStudentDetail(String id) {
        return executeWithCatch(() -> {

            StudentEntity student =
                    checkStudentExists(id);

            ProfileEntity profile =
                    checkProfileExists(student.getProfileId());

            AccountEntity account =
                    checkAccountExists(profile.getAccountId());

            StudentResponse response =
                    studentMapper.toResponse(
                            account,
                            profile,
                            student
                    );

            response.setClasses(
                    studentClassRepository.findClassesByStudentId(
                            student.getId()
                    )
            );

            return response;

        }, "Lỗi hệ thống khi lấy thông tin học viên");
    }

    @Override
    public String updateStudent(String id, StudentRequest request) {

        validateStudent(request);

        String email = getCurrentUserEmail();

        return executeWithCatch(() -> {

            StudentEntity student =
                    checkStudentExists(id);

            ProfileEntity profile =
                    checkProfileExists(student.getProfileId());

            AccountEntity account =
                    checkAccountExists(profile.getAccountId());

            studentMapper.updateAccount(account, request);
            studentMapper.updateProfile(profile, request);
            studentMapper.updateStudent(student, request);

            LocalDateTime now = LocalDateTime.now();

            account.setUpdateBy(email);
            account.setUpdateDate(now);

            profile.setUpdatedBy(email);
            profile.setUpdateDate(now);

            student.setUpdatedBy(email);
            student.setUpdateDate(now);

            // Update Account - Profile - Student
            firestore.runTransaction(transaction -> {

                transaction.set(
                        accountRepository.getAccountCollection()
                                .document(account.getId()),
                        studentMapper.accountToFirestoreMap(account),
                        SetOptions.merge());

                transaction.set(
                        profileRepository.getProfileCollection()
                                .document(profile.getId()),
                        studentMapper.profileToFirestoreMap(profile),
                        SetOptions.merge());

                transaction.set(
                        studentRepository.getStudentCollection()
                                .document(student.getId()),
                        studentMapper.studentToFirestoreMap(student),
                        SetOptions.merge());

                return null;
            }).get();

            // Xóa mềm toàn bộ lớp cũ
            studentClassRepository.softDeleteByStudentId(student.getId());

            // Thêm lại danh sách lớp mới
            if (request.getClasses() != null) {

                for (StudentClassRequest item : request.getClasses()) {

                    StudentClassEntity entity =
                            StudentClassEntity.builder()
                                    .studentId(student.getId())
                                    .classId(item.getClassId())
                                    .status(item.getStatus())
                                    .deleted(false)
                                    .createdBy(email)
                                    .updatedBy(email)
                                    .createDate(now)
                                    .updateDate(now)
                                    .build();

                    studentClassRepository.save(entity);
                }
            }

            return "Cập nhật học viên thành công!";

        }, "Lỗi hệ thống khi cập nhật học viên");
    }

    @Override
    public String deleteStudent(String id, String deletedBy) {
        return executeWithCatch(() -> {
            StudentEntity student =
                    checkStudentExists(id);
            ProfileEntity profile =
                    checkProfileExists(student.getProfileId());
            AccountEntity account =
                    checkAccountExists(profile.getAccountId());
            account.setDeleted(true);
            profile.setDeleted(true);
            student.setDeleted(true);

            account.setUpdateBy(deletedBy);
            profile.setUpdatedBy(deletedBy);
            student.setUpdatedBy(deletedBy);

            return firestore.runTransaction(transaction -> {
                transaction.set(
                        accountRepository.getAccountCollection()
                                .document(account.getId()),
                        studentMapper.accountToFirestoreMap(account),
                        SetOptions.merge());
                transaction.set(
                        profileRepository.getProfileCollection()
                                .document(profile.getId()),
                        studentMapper.profileToFirestoreMap(profile),
                        SetOptions.merge());
                transaction.set(
                        studentRepository.getStudentCollection()
                                .document(student.getId()),
                        studentMapper.studentToFirestoreMap(student),
                        SetOptions.merge());
                return "Xóa học viên thành công!";
            }).get();
        }, "Lỗi hệ thống khi xóa học viên");
    }




}