package com.lms.backend.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.lms.backend.dto.request.StudentRequest;
import com.lms.backend.dto.response.StudentResponse;
import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.ProfileEntity;
import com.lms.backend.entity.StudentEntity;
import com.lms.backend.mapper.StudentMapper;
import com.lms.backend.repository.AccountRepository;
import com.lms.backend.repository.ProfileRepository;
import com.lms.backend.repository.StudentRepository;
import com.lms.backend.service.IStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements IStudentService {

    private final Firestore firestore;

    private final StudentRepository studentRepository;

    private final ProfileRepository profileRepository;

    private final AccountRepository accountRepository;

    private final StudentMapper studentMapper;

    @Override
    public long countStudents() throws Exception {
        return studentRepository.countActive();
    }

    @Override
    public String createStudent(StudentRequest request) throws Exception {

        AccountEntity account =
                studentMapper.toAccountEntity(request);

        ProfileEntity profile =
                studentMapper.toProfileEntity(request);

        StudentEntity student =
                studentMapper.toStudentEntity(request);


        account.setDeleted(false);
        account.setCreatedBy(request.getRequestedBy());
        account.setUpdateBy(request.getRequestedBy());

        account.setCreateDate(LocalDateTime.now());
        account.setUpdateDate(LocalDateTime.now());

        account.setStatus("ACTIVE");
         account.setRoleId("STUDENT");
         account.setPassword("123456");


        profile.setDeleted(false);
        profile.setCreatedBy(request.getRequestedBy());
        profile.setUpdatedBy(request.getRequestedBy());

        profile.setCreateDate(LocalDateTime.now());
        profile.setUpdateDate(LocalDateTime.now());


        student.setDeleted(false);
        student.setCreatedBy(request.getRequestedBy());
        student.setUpdatedBy(request.getRequestedBy());

        student.setCreateDate(LocalDateTime.now());
        student.setUpdateDate(LocalDateTime.now());


        return firestore.runTransaction(transaction -> {

            // Account
            var accountRef = firestore.collection("accounts").document();
            account.setId(accountRef.getId());

            transaction.set(
                    accountRef,
                    studentMapper.accountToFirestoreMap(account)
            );

            // Profile
            var profileRef = firestore.collection("profiles").document();
            profile.setId(profileRef.getId());
            profile.setAccountId(account.getId());

            transaction.set(
                    profileRef,
                    studentMapper.profileToFirestoreMap(profile)
            );

            // Student
            var studentRef = firestore.collection("students").document();
            student.setId(studentRef.getId());
            student.setProfileId(profile.getId());

            transaction.set(
                    studentRef,
                    studentMapper.studentToFirestoreMap(student)
            );

            return student.getId();

        }).get();

    }
    @Override
    public List<StudentResponse> getAllStudents() throws Exception {

        List<StudentResponse> responses = new ArrayList<>();

        List<StudentEntity> students = studentRepository.findAllActive();

        for (StudentEntity student : students) {

            ProfileEntity profile =
                    profileRepository.findById(student.getProfileId());

            if (profile == null) {
                continue;
            }

            AccountEntity account =
                    accountRepository.findEntityById(profile.getAccountId());

            if (account == null) {
                continue;
            }

            responses.add(
                    studentMapper.toResponse(
                            account,
                            profile,
                            student
                    )
            );
        }

        return responses;
    }

    @Override
    public StudentResponse getStudentDetail(String id) throws Exception {

        StudentEntity student = studentRepository.findById(id);

        if (student == null) {
            throw new IllegalArgumentException("Không tìm thấy học viên.");
        }

        ProfileEntity profile =
                profileRepository.findById(student.getProfileId());

        if (profile == null) {
            throw new IllegalArgumentException("Không tìm thấy hồ sơ học viên.");
        }

        AccountEntity account =
                accountRepository.findEntityById(profile.getAccountId());

        if (account == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }

        return studentMapper.toResponse(
                account,
                profile,
                student
        );
    }

    @Override
    public String updateStudent(String id, StudentRequest request) throws Exception {

        StudentEntity student = studentRepository.findById(id);

        if (student == null) {
            throw new IllegalArgumentException("Không tìm thấy học viên.");
        }

        ProfileEntity profile =
                profileRepository.findById(student.getProfileId());

        if (profile == null) {
            throw new IllegalArgumentException("Không tìm thấy hồ sơ.");
        }

        AccountEntity account =
                accountRepository.findEntityById(profile.getAccountId());

        if (account == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }

        // Update entity
        studentMapper.updateAccount(account, request);
        studentMapper.updateProfile(profile, request);
        studentMapper.updateStudent(student, request);

        LocalDateTime now = LocalDateTime.now();

        account.setUpdateBy(request.getRequestedBy());
        account.setUpdateDate(now);

        profile.setUpdatedBy(request.getRequestedBy());
        profile.setUpdateDate(now);

        student.setUpdatedBy(request.getRequestedBy());
        student.setUpdateDate(now);

        return firestore.runTransaction(transaction -> {

            DocumentReference accountRef =
                    firestore.collection("accounts")
                            .document(account.getId());

            transaction.set(
                    accountRef,
                    studentMapper.accountToFirestoreMap(account),
                    SetOptions.merge()
            );

            DocumentReference profileRef =
                    firestore.collection("profiles")
                            .document(profile.getId());

            transaction.set(
                    profileRef,
                    studentMapper.profileToFirestoreMap(profile),
                    SetOptions.merge()
            );

            DocumentReference studentRef =
                    firestore.collection("students")
                            .document(student.getId());

            transaction.set(
                    studentRef,
                    studentMapper.studentToFirestoreMap(student),
                    SetOptions.merge()
            );

            return "Cập nhật học viên thành công!";

        }).get();


    }

    @Override
    public String deleteStudent(String id, String deletedBy) throws Exception {

        StudentEntity student = studentRepository.findById(id);

        if (student == null) {
            throw new IllegalArgumentException("Không tìm thấy học viên.");
        }

        ProfileEntity profile =
                profileRepository.findById(student.getProfileId());

        if (profile == null) {
            throw new IllegalArgumentException("Không tìm thấy hồ sơ.");
        }

        AccountEntity account =
                accountRepository.findEntityById(profile.getAccountId());

        if (account == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }

        LocalDateTime now = LocalDateTime.now();

        account.setDeleted(true);
        account.setUpdateBy(deletedBy);
        account.setUpdateDate(now);

        profile.setDeleted(true);
        profile.setUpdatedBy(deletedBy);
        profile.setUpdateDate(now);

        student.setDeleted(true);
        student.setUpdatedBy(deletedBy);
        student.setUpdateDate(now);

        return firestore.runTransaction(transaction -> {

            DocumentReference accountRef =
                    firestore.collection("accounts")
                            .document(account.getId());

            transaction.set(
                    accountRef,
                    studentMapper.accountToFirestoreMap(account),
                    SetOptions.merge()
            );

            DocumentReference profileRef =
                    firestore.collection("profiles")
                            .document(profile.getId());

            transaction.set(
                    profileRef,
                    studentMapper.profileToFirestoreMap(profile),
                    SetOptions.merge()
            );

            DocumentReference studentRef =
                    firestore.collection("students")
                            .document(student.getId());

            transaction.set(
                    studentRef,
                    studentMapper.studentToFirestoreMap(student),
                    SetOptions.merge()
            );

            return "Đã xóa học viên thành công!";

        }).get();
    }




}