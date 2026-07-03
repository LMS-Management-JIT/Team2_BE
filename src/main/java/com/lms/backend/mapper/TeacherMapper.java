package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.TeacherRequest;
import com.lms.backend.dto.response.TeacherResponse;
import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.ProfileEntity;
import com.lms.backend.entity.TeacherEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TeacherMapper {

    // ===================== CREATE =====================

    public AccountEntity toAccountEntity(TeacherRequest request) {

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

    public ProfileEntity toProfileEntity(TeacherRequest request) {

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

    public TeacherEntity toTeacherEntity(TeacherRequest request) {

        LocalDateTime now = LocalDateTime.now();

        return TeacherEntity.builder()
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .certifications(request.getCertifications())
                .available(true)
                .deleted(false)
                .createdBy(request.getRequestedBy())
                .updatedBy(request.getRequestedBy())
                .createDate(now)
                .updateDate(now)
                .build();
    }

    // ===================== RESPONSE =====================

    public TeacherResponse toResponse(
            AccountEntity account,
            ProfileEntity profile,
            TeacherEntity teacher) {

        if (teacher == null) {
            return null;
        }

        TeacherResponse response = new TeacherResponse();

        response.setId(teacher.getId());
        response.setProfileId(teacher.getProfileId());
        response.setSpecialization(teacher.getSpecialization());
        response.setQualification(teacher.getQualification());
        response.setCertifications(teacher.getCertifications());
        response.setAvailable(teacher.getAvailable());
        response.setDeleted(teacher.getDeleted());
        response.setCreateDate(teacher.getCreateDate());
        response.setUpdateDate(teacher.getUpdateDate());
        response.setCreatedBy(teacher.getCreatedBy());
        response.setUpdatedBy(teacher.getUpdatedBy());

        return response;
    }

    // ===================== UPDATE =====================

    public void updateAccount(AccountEntity account,
                              TeacherRequest request) {

        if (account == null || request == null) {
            return;
        }

        account.setEmail(request.getEmail());
    }

    public void updateProfile(ProfileEntity profile,
                              TeacherRequest request) {

        if (profile == null || request == null) {
            return;
        }

        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDayOfBirth(request.getDayOfBirth());
        profile.setGender(request.getGender());
        profile.setAddress(request.getAddress());
    }

    public void updateTeacher(TeacherEntity teacher,
                              TeacherRequest request) {

        if (teacher == null || request == null) {
            return;
        }

        teacher.setSpecialization(request.getSpecialization());
        teacher.setQualification(request.getQualification());
        teacher.setCertifications(request.getCertifications());

        if (request.getAvailable() != null) {
            teacher.setAvailable(request.getAvailable());
        }
    }

    // ===================== FROM DOCUMENT =====================

    public TeacherEntity fromDocument(DocumentSnapshot doc) {

        if (doc == null || !doc.exists()) {
            return null;
        }

        return TeacherEntity.builder()
                .id(doc.getId())
                .profileId(doc.getString("profile_id"))
                .specialization(doc.getString("specialization"))
                .qualification(doc.getString("qualification"))
                .certifications(doc.getString("certifications"))
                .available(doc.getBoolean("available"))
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

    // ===================== TO FIRESTORE MAP =====================

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

    public Map<String, Object> teacherToFirestoreMap(TeacherEntity entity) {

        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();

        map.put("profile_id", entity.getProfileId());
        map.put("specialization", entity.getSpecialization());
        map.put("qualification", entity.getQualification());
        map.put("certifications", entity.getCertifications());
        map.put("available", entity.getAvailable());
        map.put("deleted", entity.getDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("updated_by", entity.getUpdatedBy());
        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }

    // ===================== COMMON =====================

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