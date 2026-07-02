package com.lms.backend.utils;

import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.RoleEntity;
import com.lms.backend.repository.AccountRepository;
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
    private final PasswordEncoder passwordEncoder;

    @EventListener(ContextRefreshedEvent.class)
    public void seedData() {
        try {
            seedRoles();
            seedAdminAccount();
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

        // find ADMIN role id
        String adminRoleId = null;
        List<RoleEntity> roles = roleRepository.findAllActive();
        for (RoleEntity r : roles) {
            if ("ADMIN".equalsIgnoreCase(r.getName())) {
                adminRoleId = r.getId();
                break;
            }
        }

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
}
