package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.StudentRequest;
import com.lms.backend.dto.response.StudentResponse;
import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.ProfileEntity;
import com.lms.backend.entity.StudentEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.threeten.bp.DateTimeUtils.toLocalDateTime;

@Component
public class StudentMapper {


    public AccountEntity toAccountEntity(StudentRequest request) {

        LocalDateTime now = LocalDateTime.now();

        return AccountEntity.builder()
                .email(request.getEmail())
                .status("ACTIVE")
                .deleted(false)
                .createdBy(request.getRequestedBy())
                .updateBy(request.getRequestedBy())
                .createDate(now)
                .updateDate(now)
                .build();
    }


    public ProfileEntity toProfileEntity(StudentRequest request) {

        LocalDateTime now = LocalDateTime.now();

        return ProfileEntity.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .dayOfBirth(request.getDayOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .deleted(false)
                .createdBy(request.getRequestedBy())
                .updatedBy(request.getRequestedBy())
                .createDate(now)
                .updateDate(now)
                .build();
    }


    public StudentEntity toStudentEntity(StudentRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return StudentEntity.builder()
                .identityNumber(request.getIdentityNumber())
                .issueDate(request.getIssueDate())
                .issuePlace(request.getIssuePlace())
                .educationLevel(request.getEducationLevel())
                .interviewContent(request.getInterviewContent())
                .admissionResult(request.getAdmissionResult())
                .description(request.getDescription())
                .status("ACTIVE")
                .deleted(false)
                .createdBy(request.getRequestedBy())
                .updatedBy(request.getRequestedBy())
                .createDate(now)
                .updateDate(now)
                .build();
    }


    public StudentResponse toResponse(
            AccountEntity account,
            ProfileEntity profile,
            StudentEntity student) {

        if (student == null) {
            return null;
        }

        StudentResponse response = new StudentResponse();

        // Student
        response.setId(student.getId());
        response.setIdentityNumber(student.getIdentityNumber());
        response.setIssueDate(student.getIssueDate());
        response.setIssuePlace(student.getIssuePlace());
        response.setEducationLevel(student.getEducationLevel());
        response.setAdmissionResult(student.getAdmissionResult());
        response.setStatus(student.getStatus());

        // Profile
        if (profile != null) {
            response.setFullName(profile.getFullName());
            response.setPhoneNumber(profile.getPhoneNumber());
            response.setDayOfBirth(profile.getDayOfBirth());
            response.setGender(profile.getGender());
            response.setAddress(profile.getAddress());
        }

        // Account
        if (account != null) {
            response.setEmail(account.getEmail());
        }

        return response;
    }


    public void updateAccount(AccountEntity account,
                              StudentRequest request) {

        if (account == null || request == null) {
            return;
        }

        account.setEmail(request.getEmail());
    }


    public void updateProfile(ProfileEntity profile,
                              StudentRequest request) {

        if (profile == null || request == null) {
            return;
        }

        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDayOfBirth(request.getDayOfBirth());
        profile.setGender(request.getGender());
        profile.setAddress(request.getAddress());
    }


    public void updateStudent(StudentEntity student,
                              StudentRequest request) {

        if (student == null || request == null) {
            return;
        }

        student.setIdentityNumber(request.getIdentityNumber());
        student.setIssueDate(request.getIssueDate());
        student.setIssuePlace(request.getIssuePlace());
        student.setEducationLevel(request.getEducationLevel());
        student.setInterviewContent(request.getInterviewContent());
        student.setAdmissionResult(request.getAdmissionResult());
        student.setDescription(request.getDescription());
    }



    public StudentEntity fromDocument(DocumentSnapshot doc) {

        if (doc == null || !doc.exists()) {
            return null;
        }

        return StudentEntity.builder()
                .id(doc.getId())
                .profileId(doc.getString("profile_id"))
                .identityNumber(doc.getString("identity_number"))
                .issueDate(toLocalDateTime(doc.getDate("issue_date")))
                .issuePlace(doc.getString("issue_place"))
                .educationLevel(doc.getString("education_level"))
                .interviewContent(doc.getString("interview_content"))
                .admissionResult(doc.getString("admission_result"))
                .description(doc.getString("description"))
                .status(doc.getString("status"))
                .deleted(doc.getBoolean("deleted"))
                .createdBy(doc.getString("created_by"))
                .updatedBy(doc.getString("updated_by"))
                .createDate(toLocalDateTime(doc.getDate("create_date")))
                .updateDate(toLocalDateTime(doc.getDate("update_date")))
                .build();
    }


    public ProfileEntity profileFromDocument(DocumentSnapshot doc) {

        if (doc == null || !doc.exists()) {
            return null;
        }

        return ProfileEntity.builder()
                .id(doc.getId())
                .accountId(doc.getString("account_id"))
                .address(doc.getString("address"))
                .createDate(toLocalDateTime(doc.getDate("create_date")))
                .createdBy(doc.getString("create_by"))
                .dayOfBirth(toLocalDateTime(doc.getDate("day_of_birth")))
                .deleted(doc.getBoolean("deleted"))
                .fullName(doc.getString("full_name"))
                .gender(doc.getString("gender"))
                .phoneNumber(doc.getString("phone_number"))
                .updateDate(toLocalDateTime(doc.getDate("update_date")))
                .updatedBy(doc.getString("update_by"))
                .build();
    }


    public AccountEntity accountFromDocument(DocumentSnapshot doc) {

        if (doc == null || !doc.exists()) {
            return null;
        }

        return AccountEntity.builder()
                .id(doc.getId())
                .email(doc.getString("email"))
                .password(doc.getString("password"))
                .roleId(doc.getString("role_id"))
                .status(doc.getString("status"))
                .deleted(doc.getBoolean("deleted"))
                .createdBy(doc.getString("created_by"))
                .updateBy(doc.getString("update_by"))
                .createDate(toLocalDateTime(doc.getDate("create_date")))
                .updateDate(toLocalDateTime(doc.getDate("update_date")))
                .build();
    }

    public Map<String, Object> accountToFirestoreMap(AccountEntity entity) {

        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();

        map.put("email", entity.getEmail());
        map.put("password", entity.getPassword());
        map.put("role_id", entity.getRoleId());
        map.put("status", entity.getStatus());
        map.put("deleted", entity.getDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("update_by", entity.getUpdateBy());
        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }


    public Map<String, Object> profileToFirestoreMap(ProfileEntity entity) {

        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();

        map.put("account_id", entity.getAccountId());
        map.put("address", entity.getAddress());
        map.put("full_name", entity.getFullName());
        map.put("phone_number", entity.getPhoneNumber());
        map.put("gender", entity.getGender());
        map.put("day_of_birth", toTimestamp(entity.getDayOfBirth()));
        map.put("deleted", entity.getDeleted());
        map.put("create_by", entity.getCreatedBy());
        map.put("update_by", entity.getUpdatedBy());
        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }


    public Map<String, Object> studentToFirestoreMap(StudentEntity entity) {

        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("profile_id", entity.getProfileId());
        map.put("identity_number", entity.getIdentityNumber());
        map.put("issue_date", toTimestamp(entity.getIssueDate()));
        map.put("issue_place", entity.getIssuePlace());
        map.put("education_level", entity.getEducationLevel());
        map.put("interview_content", entity.getInterviewContent());
        map.put("admission_result", entity.getAdmissionResult());
        map.put("description", entity.getDescription());
        map.put("status", entity.getStatus());
        map.put("deleted", entity.getDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("updated_by", entity.getUpdatedBy());
        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }

    private Date toTimestamp(LocalDateTime localDateTime) {

        if (localDateTime == null) {
            return null;
        }

        return Date.from(
                localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {

        if (date == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                date.toInstant(),
                ZoneId.systemDefault());
    }
}