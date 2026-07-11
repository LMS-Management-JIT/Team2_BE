package com.lms.backend.service;

import com.lms.backend.dto.request.ModuleTopicRequest;
import com.lms.backend.dto.response.ModuleTopicResponse;

import java.util.List;

public interface IModuleTopicService {

    ModuleTopicResponse create(ModuleTopicRequest request);

    ModuleTopicResponse getById(String id);

    List<ModuleTopicResponse> getAll();

    List<ModuleTopicResponse> getByModuleId(String moduleId);

    List<ModuleTopicResponse> getByTopicId(String topicId);

    ModuleTopicResponse update(String id, ModuleTopicRequest request);

    void delete(String id);
}