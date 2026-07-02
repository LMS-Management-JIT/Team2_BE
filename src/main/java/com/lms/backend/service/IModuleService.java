package com.lms.backend.service;

import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IModuleService {
    long countModules() throws Exception;
    // 1. Lấy danh sách tất cả module đang hoạt động
    List<ModuleResponse> getAllActiveModules() throws ExecutionException, InterruptedException;

    // 2. Tạo mới một module
    ModuleResponse createModule(ModuleRequest requestDto, String creatorId) throws ExecutionException, InterruptedException;

    // 3. Cập nhật thông tin module
    ModuleResponse updateModule(String id, ModuleRequest requestDto, String updaterId) throws ExecutionException, InterruptedException;

    // 4. Xóa mềm một module(đổi trạng thái deleted = true)
    void softDeleteModule(String id, String updaterId) throws ExecutionException, InterruptedException;
}