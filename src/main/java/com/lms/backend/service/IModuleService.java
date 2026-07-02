package com.lms.backend.service;

import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;

import java.util.List;

public interface IModuleService {
    long countModules() throws Exception;
    List<ModuleResponse> getAllActiveModules();

    ModuleResponse createModule(ModuleRequest request, String userId);

    ModuleResponse updateModule(String id, ModuleRequest request, String userId);

    void softDeleteModule(String id, String userId);
}