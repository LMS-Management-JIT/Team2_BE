package com.lms.backend.service;

import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Map;

public interface IModuleService {
    long countModules() throws Exception;

    Map<String, Object> getAllActiveModules(int page, int size, String name, String courseName, String status, String createDate);

    String createModule(ModuleRequest request, String userId);

    String updateModule(String id, ModuleRequest request, String userId);

    void softDeleteModule(String id, String userId);
}