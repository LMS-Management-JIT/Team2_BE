package com.lms.backend.service.impl;

import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;
import com.lms.backend.entity.ModuleEntity;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.ModuleMapper;
import com.lms.backend.repository.ModuleRepository;
import com.lms.backend.service.IModuleService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows; // Import SneakyThrows
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements IModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;


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
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (authentication != null && authentication.getPrincipal() instanceof String principalName && !"anonymousUser".equals(principalName)) {
            return principalName;
        }
        return "system";
    }

    @Override
    @SneakyThrows // Thay thế cho try-catch lỗi 500 của Firebase
    public long countModules() {
        return moduleRepository.countActive();
    }

    @Override
    @SneakyThrows
    public String createModule(ModuleRequest request) {
        String currentUser = request.getRequestedBy() != null ? request.getRequestedBy() : getCurrentUserEmail();

        ModuleEntity entity = moduleMapper.toEntity(request);
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        entity.setCreatedBy(currentUser);
        entity.setUpdateBy(currentUser);

        return moduleRepository.save(entity);
    }

    @Override
    @SneakyThrows
    public List<ModuleResponse> getAllModules() {
        com.lms.backend.dto.response.PagedResponse<ModuleEntity> result = moduleRepository.findAllActive(0, Integer.MAX_VALUE, null, null);
        return result.getContent().stream().map(moduleMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public PagedResponse<ModuleResponse> getAllModulesPage(int page, int size, String name, String status) {
        return executeWithCatch(() -> {
            PagedResponse<ModuleEntity> entityPage = moduleRepository.findAllActive(page, size, name, status);
            List<ModuleResponse> responseList = entityPage.getContent().stream()
                    .map(moduleMapper::toResponse)
                    .collect(Collectors.toList());

            return new PagedResponse<>(responseList, entityPage.getTotalElements(), entityPage.getPage(), entityPage.getSize(), entityPage.getTotalPages());
        }, "Lỗi khi lấy danh sách Module theo trang");
    }

    @Override
    @SneakyThrows
    public ModuleResponse getModuleById(String id) {
        ModuleEntity entity = moduleRepository.findById(id);
        if (entity == null || entity.isDeleted()) {
            // Ném ngoại lệ, GlobalExceptionHandler sẽ tự động bắt và trả về JSON mã 404
            throw new IllegalArgumentException("Không tìm thấy Module hoặc Module đã bị xóa!");
        }
        return moduleMapper.toResponse(entity);
    }

    @Override
    @SneakyThrows
    public String updateModule(String id, ModuleRequest request) {
        ModuleEntity existingEntity = moduleRepository.findById(id);
        if (existingEntity == null || existingEntity.isDeleted()) {
            // Ném ngoại lệ ra cho Advice xử lý
            throw new IllegalArgumentException("Không tìm thấy Module để cập nhật!");
        }

        String currentUser = request.getRequestedBy() != null ? request.getRequestedBy() : getCurrentUserEmail();

        moduleMapper.updateEntityFromRequest(request, existingEntity);
        existingEntity.setUpdateDate(LocalDateTime.now());
        existingEntity.setUpdateBy(currentUser);

        return moduleRepository.update(id, existingEntity);
    }

    @Override
    @SneakyThrows
    public boolean existsById(String moduleId) {
        ModuleEntity entity = moduleRepository.findById(moduleId);
        return entity != null && !entity.isDeleted();
    }

    @Override
    @SneakyThrows
    public String deleteModule(String id, String deletedBy) {
        ModuleEntity existingEntity = moduleRepository.findById(id);
        if (existingEntity == null) {
            // Ném ngoại lệ ra cho Advice xử lý
            throw new IllegalArgumentException("Không tìm thấy Module để xóa!");
        }

        String user = deletedBy != null ? deletedBy : getCurrentUserEmail();
        return moduleRepository.softDelete(id, user);
    }
}