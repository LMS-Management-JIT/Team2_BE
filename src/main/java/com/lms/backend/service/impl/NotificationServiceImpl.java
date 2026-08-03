package com.lms.backend.service.impl;

import com.lms.backend.dto.request.SendNotificationRequest;
import com.lms.backend.dto.response.AccountOptionResponse;
import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.NotificationEntity;
import com.lms.backend.entity.ProfileEntity;
import com.lms.backend.entity.RoleEntity;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.kafka.producer.NotificationKafkaProducer;
import com.lms.backend.repository.AccountRepository;
import com.lms.backend.repository.NotificationRepository;
import com.lms.backend.repository.ProfileRepository;
import com.lms.backend.repository.RoleRepository;
import com.lms.backend.service.INotificationService;
import com.lms.backend.service.IOneSignalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationKafkaProducer notificationKafkaProducer;
    private final ProfileRepository profileRepository;

    @Override
    public String sendNotification(SendNotificationRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Tiêu đề thông báo không được để trống.");
        }
        if (request.getBody() == null || request.getBody().isBlank()) {
            throw new BadRequestException("Nội dung thông báo không được để trống.");
        }

        String senderEmail = getCurrentUserEmail();

        return executeWithCatch(() -> {
            List<String> recipientAccountIds = resolveRecipients(request);

            if (recipientAccountIds.isEmpty()) {
                throw new BadRequestException("Không tìm thấy người nhận phù hợp.");
            }

            // 1. Lưu log thông báo
            NotificationEntity logEntity = NotificationEntity.builder()
                    .title(request.getTitle())
                    .body(request.getBody())
                    .note(request.getNote())
                    .type("SYSTEM")
                    .targetType(request.getTargetType())
                    .targetIds(request.getTargetIds())
                    .senderId(senderEmail)
                    .build();
            String notificationId = notificationRepository.saveNotificationLog(logEntity);

            // 2. Ghi vào hộp thư từng người nhận
            for (String accountId : recipientAccountIds) {
                notificationRepository.pushToUserInbox(
                        accountId, request.getTitle(), request.getBody(), request.getNote() , "SYSTEM", null, null, notificationId
                );
            }

            // 3. Gửi push qua OneSignal (external_user_id = accountId)
            notificationKafkaProducer.publish(
                    notificationId,
                    recipientAccountIds,
                    request.getTitle(),
                    request.getBody(),
                    Map.of(
                            "type", "SYSTEM",
                            "notification_id", notificationId
                    )
            );

            return "Đã gửi thông báo tới " + recipientAccountIds.size() + " người nhận.";
        }, "Lỗi hệ thống khi gửi thông báo");
    }

    @Override
    public List<Map<String, Object>> getUserInbox(String accountId, String type) {
        return executeWithCatch(() -> {
            if (type == null || type.isBlank()) {
                return notificationRepository.findAllUserInbox(accountId);
            }
            return notificationRepository.findUserInboxByType(accountId, type);
        }, "Lỗi hệ thống khi lấy hộp thư thông báo");
    }

    private List<String> resolveRecipients(SendNotificationRequest request) throws Exception {
        String targetType = request.getTargetType();

        if (targetType == null || targetType.isBlank()) {
            throw new BadRequestException("Thiếu targetType (ALL | STUDENTS | TEACHERS | CUSTOM).");
        }

        if ("CUSTOM".equalsIgnoreCase(targetType)) {
            if (request.getTargetIds() == null || request.getTargetIds().isEmpty()) {
                throw new BadRequestException("Cần chọn ít nhất 1 người nhận cho targetType = CUSTOM.");
            }
            return request.getTargetIds();
        }

        List<AccountEntity> allAccounts = accountRepository.findAllActiveEntities();

        if ("ALL".equalsIgnoreCase(targetType)) {
            return allAccounts.stream().map(AccountEntity::getId).toList();
        }

        if ("STUDENTS".equalsIgnoreCase(targetType) || "TEACHERS".equalsIgnoreCase(targetType)) {
            String roleName = "STUDENTS".equalsIgnoreCase(targetType) ? "STUDENT" : "TEACHER";
            RoleEntity role = roleRepository.findByName(roleName);
            if (role == null) {
                throw new BadRequestException("Không tìm thấy role " + roleName + ".");
            }
            return allAccounts.stream()
                    .filter(a -> role.getId().equals(a.getRoleId()))
                    .map(AccountEntity::getId)
                    .toList();
        }

        throw new BadRequestException("targetType không hợp lệ. Chỉ chấp nhận: ALL | STUDENTS | TEACHERS | CUSTOM.");
    }

    @Override
    public List<AccountOptionResponse> getSelectableAccounts(String roleFilter) {
        try {
            List<AccountEntity> accounts = accountRepository.findAllActiveEntities();
            List<ProfileEntity> profiles = profileRepository.findAllActive();

            Map<String, ProfileEntity> profileByAccountId = profiles.stream()
                    .filter(p -> p.getAccountId() != null)
                    .collect(Collectors.toMap(ProfileEntity::getAccountId, p -> p, (a, b) -> a));

            List<AccountOptionResponse> result = new ArrayList<>();

            for (AccountEntity account : accounts) {
                RoleEntity role = null;
                if (account.getRoleId() != null) {
                    role = roleRepository.findById(account.getRoleId()).orElse(null);
                }
                String roleName = role != null ? role.getName() : "";

                if (roleFilter != null && !roleFilter.isBlank() && !roleFilter.equalsIgnoreCase(roleName)) {
                    continue;
                }

                if ("ADMIN".equalsIgnoreCase(roleName)) continue;

                ProfileEntity profile = profileByAccountId.get(account.getId());

                result.add(AccountOptionResponse.builder()
                        .accountId(account.getId())
                        .fullName(profile != null ? profile.getFullName() : "")
                        .phoneNumber(profile != null ? profile.getPhoneNumber() : "")
                        .email(account.getEmail())
                        .roleName(roleName)
                        .build());
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getMyNotifications(String type) {
        String userEmail = getCurrentUserEmail();

        // Tìm AccountEntity bằng Email để lấy accountId
        Optional<AccountEntity> account = accountRepository.findByUsername(userEmail);
        if (!account.isPresent()) {
            throw new BadRequestException("Không tìm thấy thông tin tài khoản.");
        }

        return getUserInbox(account.get().getId(), type);
    }

    @Override
    public void markNotificationAsRead(String type, String notificationDocId) {
        String userEmail = getCurrentUserEmail();
        Optional<AccountEntity> account = accountRepository.findByUsername(userEmail);
        if (!account.isPresent()) {
            throw new BadRequestException("Không tìm thấy thông tin tài khoản.");
        }

        notificationRepository.markAsRead(account.get().getId(), type, notificationDocId);
    }

    @Override
    public void markAllNotificationsAsRead() {
        String userEmail = getCurrentUserEmail();
        Optional<AccountEntity> account = accountRepository.findByUsername(userEmail);
        if (!account.isPresent()) {
            throw new BadRequestException("Không tìm thấy thông tin tài khoản.");
        }

        notificationRepository.markAllAsRead(account.get().getId());
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

    private <T> T executeWithCatch(Callable<T> task, String errorMessage) {
        try {
            return task.call();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(errorMessage + ": " + e.getMessage(), e);
        }
    }
}