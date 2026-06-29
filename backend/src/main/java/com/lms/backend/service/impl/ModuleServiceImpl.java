package com.lms.backend.service.impl;

import com.lms.backend.dto.request.ModuleRequestDto;
import com.lms.backend.dto.response.ModuleResponseDto;
import com.lms.backend.entity.Module;
import com.lms.backend.mapper.ModuleMapper;
import com.lms.backend.repository.ModuleRepository;
import com.lms.backend.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleMapper mapper;

    @Override
    public List<ModuleResponseDto> getAllActiveModules() throws ExecutionException, InterruptedException {
        return moduleRepository.findAllActive().stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ModuleResponseDto createModule(ModuleRequestDto requestDto, String creatorId) throws ExecutionException, InterruptedException {
        String uniqueId = UUID.randomUUID().toString();

        Module entity = new Module();
        entity.setId(uniqueId);
        entity.setModuleName(requestDto.moduleName());
        entity.setModuleCode(requestDto.moduleCode());
        entity.setDescription(requestDto.description());
        entity.setDeleted(false);
        entity.setCreateDate(com.google.cloud.Timestamp.now());
        entity.setCreatedBy(creatorId);

        moduleRepository.save(entity);
        return mapper.toResponseDto(entity);
    }

    @Override
    public ModuleResponseDto updateModule(String id, ModuleRequestDto requestDto, String updaterId) throws ExecutionException, InterruptedException {
        Module entity = moduleRepository.findById(id);
        if (entity == null || entity.isDeleted()) {
            throw new IllegalArgumentException("Học phần không tồn tại hoặc đã bị xóa!");
        }

        entity.setModuleName(requestDto.moduleName());
        entity.setModuleCode(requestDto.moduleCode());
        entity.setDescription(requestDto.description());
        entity.setUpdateDate(com.google.cloud.Timestamp.now());
        entity.setUpdateBy(updaterId);

        moduleRepository.save(entity);
        return mapper.toResponseDto(entity);
    }

    @Override
    public void softDeleteModule(String id, String updaterId) throws ExecutionException, InterruptedException {
        Module entity = moduleRepository.findById(id);
        if (entity == null || entity.isDeleted()) {
            throw new IllegalArgumentException("Học phần không tồn tại hoặc đã bị xóa!");
        }

        entity.setDeleted(true);
        entity.setUpdateDate(com.google.cloud.Timestamp.now());
        entity.setUpdateBy(updaterId);

        moduleRepository.save(entity);
    }
}