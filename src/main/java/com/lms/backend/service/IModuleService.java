package com.lms.backend.service;

import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;

import java.util.List;

public interface IModuleService {
    long countModules();
    String createModule(ModuleRequest request);
    List<ModuleResponse> getAllModules();
    PagedResponse<ModuleResponse> getAllModulesPage(int page, int size, String name, String status);

    ModuleResponse getModuleById(String id);
    String updateModule(String id, ModuleRequest request);
    String deleteModule(String id, String deletedBy);
}