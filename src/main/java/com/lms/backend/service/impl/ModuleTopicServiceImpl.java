package com.lms.backend.service.impl;

import com.lms.backend.dto.request.ModuleTopicRequest;
import com.lms.backend.dto.response.ModuleTopicResponse;
import com.lms.backend.entity.ModuleTopicEntity;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.ModuleTopicMapper;
import com.lms.backend.repository.ModuleTopicRepository;
import com.lms.backend.service.IModuleService;
import com.lms.backend.service.IModuleTopicService;
import com.lms.backend.service.ITopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleTopicServiceImpl implements IModuleTopicService {

    private final ModuleTopicRepository moduleTopicRepository;
    private final ModuleTopicMapper moduleTopicMapper;
    private final IModuleService moduleService;
    private final ITopicService topicService;

    // ── Helper: enrich ModuleTopicResponse with topicName ──
    private ModuleTopicResponse enrichWithTopicName(ModuleTopicResponse response) {
        if (response == null) return null;
        try {
            var topic = topicService.getById(response.getTopicId());
            response.setTopicName(topic.getTopicName());
        } catch (Exception e) {
            // silently skip if topic not found
        }
        return response;
    }

    private List<ModuleTopicResponse> enrichList(List<ModuleTopicResponse> list) {
        return list.stream()
                .map(this::enrichWithTopicName)
                .collect(Collectors.toList());
    }

    @Override
    public ModuleTopicResponse create(ModuleTopicRequest request) {
        // Kiểm tra tồn tại
        try {
            if (!moduleService.existsById(request.getModuleId())) {
                throw new ResourceNotFoundException("Module not found with id: " + request.getModuleId());
            }
            if (!topicService.existsById(request.getTopicId())) {
                throw new ResourceNotFoundException("Topic not found with id: " + request.getTopicId());
            }
        } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
            throw new RuntimeException("Error validating existence: " + e.getMessage(), e);
        }

        // Kiểm tra trùng lặp
        if (moduleTopicRepository.exists(request.getModuleId(), request.getTopicId())) {
            throw new IllegalArgumentException("Topic already associated with this module");
        }

        // Tự động tính order
        long count = moduleTopicRepository.countByModuleId(request.getModuleId());
        int nextOrder = (int) count + 1;

        ModuleTopicEntity entity = moduleTopicMapper.toEntity(request);
        entity.setTopicOrder(nextOrder);
        entity.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        entity.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        entity.setDeleted(false);

        ModuleTopicEntity saved = moduleTopicRepository.save(entity);
        return enrichWithTopicName(moduleTopicMapper.toResponse(saved));
    }

    @Override
    public ModuleTopicResponse getById(String id) {

        ModuleTopicEntity entity = moduleTopicRepository.findById(id);

        if (entity == null) {
            throw new NoSuchElementException("ModuleTopic relation not found.");
        }

        return enrichWithTopicName(moduleTopicMapper.toResponse(entity));
    }

    @Override
    public List<ModuleTopicResponse> getAll() {

        return enrichList(
                moduleTopicRepository.findAll()
                        .stream()
                        .map(moduleTopicMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<ModuleTopicResponse> getByModuleId(String moduleId) {

        return enrichList(
                moduleTopicRepository.findByModuleId(moduleId)
                        .stream()
                        .map(moduleTopicMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<ModuleTopicResponse> getByTopicId(String topicId) {

        return enrichList(
                moduleTopicRepository.findByTopicId(topicId)
                        .stream()
                        .map(moduleTopicMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public ModuleTopicResponse update(String id, ModuleTopicRequest request) {

        ModuleTopicEntity entity = moduleTopicRepository.findById(id);

        if (entity == null) {
            throw new NoSuchElementException("ModuleTopic relation not found.");
        }

        moduleTopicMapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        ModuleTopicEntity updated = moduleTopicRepository.save(entity);
        return enrichWithTopicName(moduleTopicMapper.toResponse(updated));
    }

    @Override
    public void delete(String id) {

        ModuleTopicEntity entity = moduleTopicRepository.findById(id);
        if (entity == null) {
            throw new NoSuchElementException("ModuleTopic relation not found.");
        }
        moduleTopicRepository.deleteById(id);
    }

    @Override
    public ModuleTopicResponse addTopicToModule(String moduleId, String topicId, String createdBy)
            throws ExecutionException, InterruptedException {

        // Business checks
        if (!moduleService.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module not found with id: " + moduleId);
        }
        if (!topicService.existsById(topicId)) {
            throw new ResourceNotFoundException("Topic not found with id: " + topicId);
        }
        if (moduleTopicRepository.exists(moduleId, topicId)) {
            throw new IllegalArgumentException("Topic already associated with this module");
        }

        // Auto-assign order
        long count = moduleTopicRepository.countByModuleId(moduleId);
        int nextOrder = (int) count + 1;

        ModuleTopicEntity entity = ModuleTopicEntity.builder()
                .moduleId(moduleId)
                .topicId(topicId)
                .topicOrder(nextOrder)
                .deleted(false)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .updatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();

        ModuleTopicEntity saved = moduleTopicRepository.save(entity);
        return enrichWithTopicName(moduleTopicMapper.toResponse(saved));
    }

    @Override
    public void removeTopicFromModule(String moduleId, String topicId, String updatedBy) {
        ModuleTopicEntity mapping = moduleTopicRepository
                .findByModuleIdAndTopicId(moduleId, topicId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mapping not found for module: " + moduleId + " and topic: " + topicId));

        moduleTopicRepository.deleteById(mapping.getId());

        // Reorder remaining topics in the module
        List<ModuleTopicEntity> remaining = moduleTopicRepository.findByModuleId(moduleId);
        for (int i = 0; i < remaining.size(); i++) {
            ModuleTopicEntity rel = remaining.get(i);
            if (!rel.getId().equals(mapping.getId())) {
                moduleTopicRepository.updateTopicOrder(rel.getId(), i + 1);
            }
        }
    }

    @Override
    public void reorderTopics(String moduleId, List<String> topicIds, String updatedBy)
            throws ExecutionException, InterruptedException {

        if (topicIds == null || topicIds.isEmpty()) {
            return;
        }

        // Validate module exists
        if (!moduleService.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module not found");
        }

        // Update order
        for (int i = 0; i < topicIds.size(); i++) {
            String topicId = topicIds.get(i);
            ModuleTopicEntity mapping = moduleTopicRepository
                    .findByModuleIdAndTopicId(moduleId, topicId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Topic not found in module: " + topicId));

            moduleTopicRepository.updateTopicOrder(mapping.getId(), i + 1);
        }
    }
}