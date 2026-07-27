package com.lms.backend.service;

import com.lms.backend.dto.request.ModuleTopicRequest;
import com.lms.backend.dto.request.ReorderTopicsRequest;
import com.lms.backend.dto.response.ModuleTopicResponse;
import com.lms.backend.dto.response.TopicResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IModuleTopicService {

    ModuleTopicResponse create(ModuleTopicRequest request);

    ModuleTopicResponse getById(String id);

    List<ModuleTopicResponse> getAll();

    List<ModuleTopicResponse> getByModuleId(String moduleId);

    List<ModuleTopicResponse> getByTopicId(String topicId);

    ModuleTopicResponse update(String id, ModuleTopicRequest request);

    void delete(String id);

    // New standardized methods
    ModuleTopicResponse addTopicToModule(String moduleId, String topicId, String createdBy)
            throws ExecutionException, InterruptedException;

    void removeTopicFromModule(String moduleId, String topicId, String updatedBy);

    void reorderTopics(String moduleId, List<String> topicIds, String updatedBy)
            throws ExecutionException, InterruptedException;
}