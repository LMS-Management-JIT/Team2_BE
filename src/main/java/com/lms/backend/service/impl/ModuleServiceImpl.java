package com.lms.backend.service.impl;

import com.lms.backend.dto.request.ModuleRequestDto;
import com.lms.backend.dto.response.ModuleResponseDto;
import com.lms.backend.entity.ModuleEntity;
import com.lms.backend.mapper.ModuleMapper;
import com.lms.backend.repository.ModuleRepository;
import com.lms.backend.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper mapper;

    @Override
    public List<ModuleResponseDto> getAllActiveModules() throws ExecutionException, InterruptedException {
        return moduleRepository.findAllActive().stream()
                .map(mapper::toResponseDto)
                .toList(); // Tối ưu hóa Java 21 (.toList() thay thế Collectors.toList())
    }

    @Override
    public ModuleResponseDto createModule(ModuleRequestDto requestDto, String creatorId) throws ExecutionException, InterruptedException {
        String uniqueId = UUID.randomUUID().toString();

        ModuleEntity entity = ModuleEntity.builder()
                .id(uniqueId)
                .moduleName(requestDto.moduleName())
                .moduleCode(requestDto.moduleCode())
                .description(requestDto.description())
                .deleted(false)
                .createDate(LocalDateTime.now())
                .createdBy(creatorId)
                .build();

        moduleRepository.save(entity);
        return mapper.toResponseDto(entity);
    }

    @Override
    public ModuleResponseDto updateModule(String id, ModuleRequestDto requestDto, String updaterId) throws ExecutionException, InterruptedException {
        ModuleEntity entity = moduleRepository.findById(id);
        if (entity == null || entity.isDeleted()) {
            throw new IllegalArgumentException("module không tồn tại hoặc đã bị xóa!");
        }

        entity.setModuleName(requestDto.moduleName());
        entity.setModuleCode(requestDto.moduleCode());
        entity.setDescription(requestDto.description());
        entity.setUpdateDate(LocalDateTime.now());
        entity.setUpdateBy(updaterId);

        moduleRepository.save(entity);
        return mapper.toResponseDto(entity);
    }

    @Override
    public void softDeleteModule(String id, String updaterId) throws ExecutionException, InterruptedException {
        ModuleEntity entity = moduleRepository.findById(id);
        if (entity == null || entity.isDeleted()) {
            throw new IllegalArgumentException("module không tồn tại hoặc đã bị xóa!");
        }

        entity.setDeleted(true);
        entity.setUpdateDate(LocalDateTime.now());
        entity.setUpdateBy(updaterId);

        moduleRepository.save(entity);
    }
}