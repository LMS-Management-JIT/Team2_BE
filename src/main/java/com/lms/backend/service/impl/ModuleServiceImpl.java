package com.lms.backend.service.impl;

import com.lms.backend.dto.request.HistoryRequest;
import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;
import com.lms.backend.entity.ActionType;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.entity.EntityType;
import com.lms.backend.entity.ModuleEntity;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.ModuleMapper;
import com.lms.backend.repository.ModuleRepository;
import com.lms.backend.service.IHistoryService;
import com.lms.backend.service.IModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements IModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    private final IHistoryService historyService;


    private <T> T executeWithCatch(Callable<T> task, String errorMessage) {
        try {
            return task.call();
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(errorMessage + ": " + e.getMessage(), e);
        }
    }

    @Override
    public long countModules() throws Exception {
        return moduleRepository.countActive();
    }
    @Override
    public List<ModuleResponse> getAllActiveModules() {
        return moduleRepository.findAllActive()
                .stream()
                .map(ModuleMapper::toResponse)
                .toList();
    }

    @Override
    public String createModule(ModuleRequest request, String userId) {
        String email = getCurrentUserEmail();
        String newId = executeWithCatch(() -> {
            ModuleEntity entity = moduleMapper.toEntity(request);
            entity.setCreateDate(LocalDateTime.now());
            entity.setCreatedBy(email);
            entity.setDeleted(false);
            return moduleRepository.save(entity);
        }, "Lỗi hệ thống khi tạo Module");

        historyService.record(HistoryRequest.builder()
                .action(ActionType.ADD)
                .actorId(email)
                .actorName(email)
                .entityType(EntityType.CLASS.name())
                .entityId(newId)
                .entityName(request.getModuleName())
                .changes(Map.of())
                .build());

        return newId;
    }

    @Override
    public ModuleResponse updateModule(String id, ModuleRequest request, String userId) {
        ModuleEntity existingEntity = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay module voi id: " + id));

        ModuleMapper.updateEntityFromRequest(request, existingEntity);

        ModuleEntity updatedEntity = moduleRepository.update(id, existingEntity, userId);
        return ModuleMapper.toResponse(updatedEntity);
    }

    @Override
    public void softDeleteModule(String id, String userId) {
        moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay module voi id: " + id));

        moduleRepository.softDelete(id, userId);
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

        if (principal instanceof String principalName &&
                !"anonymousUser".equals(principalName)) {
            return principalName;
        }

        throw new IllegalStateException("Authentication required");
    }
}