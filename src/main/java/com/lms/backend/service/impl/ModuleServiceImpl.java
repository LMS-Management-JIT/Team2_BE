package com.lms.backend.service.impl;

import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;
import com.lms.backend.entity.ModuleEntity;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.ModuleMapper;
import com.lms.backend.repository.ModuleRepository;
import com.lms.backend.service.IModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements IModuleService {

    private final ModuleRepository moduleRepository;

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
    public ModuleResponse createModule(ModuleRequest request, String userId) {
        ModuleEntity entity = ModuleMapper.toEntity(request);
        ModuleEntity savedEntity = moduleRepository.save(entity, userId);
        return ModuleMapper.toResponse(savedEntity);
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
}