package com.lms.backend.service.impl;

import com.lms.backend.dto.request.ModuleTopicRequest;
import com.lms.backend.dto.response.ModuleTopicResponse;
import com.lms.backend.entity.ModuleTopicEntity;
import com.lms.backend.mapper.ModuleTopicMapper;
import com.lms.backend.repository.ModuleTopicRepository;
import com.lms.backend.service.IModuleTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleTopicServiceImpl implements IModuleTopicService {

    private final ModuleTopicRepository moduleTopicRepository;
    private final ModuleTopicMapper moduleTopicMapper;

    @Override
    public ModuleTopicResponse create(ModuleTopicRequest request) {

        ModuleTopicEntity entity = moduleTopicMapper.toEntity(request);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(false);

        ModuleTopicEntity saved = moduleTopicRepository.save(entity);

        return moduleTopicMapper.toResponse(saved);
    }

    @Override
    public ModuleTopicResponse getById(String id) {

        ModuleTopicEntity entity = moduleTopicRepository.findById(id);

        if (entity == null) {
            throw new NoSuchElementException("ModuleTopic relation not found.");
        }

        return moduleTopicMapper.toResponse(entity);
    }

    @Override
    public List<ModuleTopicResponse> getAll() {

        return moduleTopicRepository.findAll()
                .stream()
                .map(moduleTopicMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModuleTopicResponse> getByModuleId(String moduleId) {

        return moduleTopicRepository.findByModuleId(moduleId)
                .stream()
                .map(moduleTopicMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModuleTopicResponse> getByTopicId(String topicId) {

        return moduleTopicRepository.findByTopicId(topicId)
                .stream()
                .map(moduleTopicMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ModuleTopicResponse update(String id, ModuleTopicRequest request) {

        ModuleTopicEntity entity = moduleTopicRepository.findById(id);

        if (entity == null) {
            throw new NoSuchElementException("ModuleTopic relation not found.");
        }

        moduleTopicMapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        ModuleTopicEntity updated = moduleTopicRepository.save(entity);
        return moduleTopicMapper.toResponse(updated);
    }

    @Override
    public void delete(String id) {

        ModuleTopicEntity entity = moduleTopicRepository.findById(id);
        if (entity == null) {
            throw new NoSuchElementException("ModuleTopic relation not found.");
        }
        moduleTopicRepository.deleteById(id);
    }
}