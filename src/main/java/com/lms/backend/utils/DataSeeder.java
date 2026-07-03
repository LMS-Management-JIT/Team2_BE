package com.lms.backend.utils;

import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.ProfileEntity;
import com.lms.backend.entity.RoleEntity;
import com.lms.backend.entity.StudentEntity;
import com.lms.backend.repository.AccountRepository;
import com.lms.backend.repository.ProfileRepository;
import com.lms.backend.repository.StudentRepository;
import com.lms.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ContextRefreshedEvent.class)
    public void seedData() {
        try {
            seedRoles();
            seedAdminAccount();
            seedStudentAccount();
        } catch (Exception e) {
            log.error("Failed to seed initial data", e);
        }
    }

    private void seedRoles() throws ExecutionException, InterruptedException {
        List<RoleEntity> existing = roleRepository.findAllActive();

        boolean hasAdmin = existing.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()));
        boolean hasTeacher = existing.stream().anyMatch(r -> "TEACHER".equalsIgnoreCase(r.getName()));
        boolean hasStudent = existing.stream().anyMatch(r -> "STUDENT".equalsIgnoreCase(r.getName()));

        if (!hasAdmin) {
            RoleEntity admin = RoleEntity.builder()
                    .name("ADMIN")
                    .deleted(false)
                    .createdBy("system")
                    .build();
            String id = roleRepository.save(admin);
            log.info("Created role ADMIN with id={}", id);
        }
        if (!hasTeacher) {
            RoleEntity teacher = RoleEntity.builder()
                    .name("TEACHER")
                    .deleted(false)
                    .createdBy("system")
                    .build();
            String id = roleRepository.save(teacher);
            log.info("Created role TEACHER with id={}", id);
        }
        if (!hasStudent) {
            RoleEntity student = RoleEntity.builder()
                    .name("STUDENT")
                    .deleted(false)
                    .createdBy("system")
                    .build();
            String id = roleRepository.save(student);
            log.info("Created role STUDENT with id={}", id);
        }
    }

    private void seedAdminAccount() throws Exception {
        String adminEmail = "admin@lms.com";
        if (accountRepository.findByUsername(adminEmail).isPresent()) {
            log.info("Admin account already exists: {}", adminEmail);
            return;
        }

        String adminRoleId = findRoleIdByName("ADMIN");

        if (adminRoleId == null) {
            log.warn("ADMIN role not found, creating it now");
            RoleEntity adminRole = RoleEntity.builder()
                    .name("ADMIN")
                    .deleted(false)
                    .createdBy("system")
                    .build();
            adminRoleId = roleRepository.save(adminRole);
        }

        AccountEntity admin = AccountEntity.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode("admin"))
                .roleId(adminRoleId)
                .status("ACTIVE")
                .deleted(false)
                .createdBy("system")
                .build();

        String accountId = accountRepository.save(admin);
        log.info("Created admin account {} with id={}", adminEmail, accountId);
    }

    private void seedStudentAccount() throws Exception {
        String studentEmail = "student@lms.com";
        if (accountRepository.findByUsername(studentEmail).isPresent()) {
            log.info("Student account already exists: {}", studentEmail);
            return;
        }

        String studentRoleId = findRoleIdByName("STUDENT");
        if (studentRoleId == null) {
            log.warn("STUDENT role not found, creating it now");
            RoleEntity studentRole = RoleEntity.builder()
                    .name("STUDENT")
                    .deleted(false)
                    .createdBy("system")
                    .build();
            studentRoleId = roleRepository.save(studentRole);
        }

        LocalDateTime now = LocalDateTime.now();

        AccountEntity account = AccountEntity.builder()
                .email(studentEmail)
                .password(passwordEncoder.encode("student123"))
                .roleId(studentRoleId)
                .status("ACTIVE")
                .deleted(false)
                .createdBy("system")
                .updateBy("system")
                .createDate(now)
                .updateDate(now)
                .build();

        String accountId = accountRepository.save(account);
        log.info("Created student account {} with id={}", studentEmail, accountId);

        ProfileEntity profile = ProfileEntity.builder()
                .accountId(accountId)
                .fullName("Student Demo")
                .phoneNumber("0900000000")
                .gender("Male")
                .address("Hanoi")
                .deleted(false)
                .createdBy("system")
                .updatedBy("system")
                .createDate(now)
                .updateDate(now)
                .build();

        String profileId = profileRepository.save(profile);
        log.info("Created student profile with id={}", profileId);

        StudentEntity student = StudentEntity.builder()
                .profileId(profileId)
                .identityNumber("STU-0001")
                .issuePlace("Hanoi")
                .educationLevel("High School")
                .interviewContent("Seeded student record")
                .admissionResult("PASSED")
                .description("Initial seeded student")
                .status("ACTIVE")
                .deleted(false)
                .createdBy("system")
                .updatedBy("system")
                .createDate(now)
                .updateDate(now)
                .build();

        String studentId = studentRepository.save(student);
        log.info("Created student record with id={}", studentId);
    }

    private String findRoleIdByName(String roleName) throws Exception {
        List<RoleEntity> roles = roleRepository.findAllActive();
        for (RoleEntity role : roles) {
            if (roleName.equalsIgnoreCase(role.getName())) {
                return role.getId();
            }
        }

        return null;
    }
}
