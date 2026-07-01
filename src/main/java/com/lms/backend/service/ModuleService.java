package com.lms.backend.service;

import com.lms.backend.dto.request.ModuleRequestDto;
import com.lms.backend.dto.response.ModuleResponseDto;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ModuleService {
    // 1. Lấy danh sách tất cả module đang hoạt động
    List<ModuleResponseDto> getAllActiveModules() throws ExecutionException, InterruptedException;

    // 2. Tạo mới một module
    ModuleResponseDto createModule(ModuleRequestDto requestDto, String creatorId) throws ExecutionException, InterruptedException;

    // 3. Cập nhật thông tin module
    ModuleResponseDto updateModule(String id, ModuleRequestDto requestDto, String updaterId) throws ExecutionException, InterruptedException;

    // 4. Xóa mềm một module(đổi trạng thái deleted = true)
    void softDeleteModule(String id, String updaterId) throws ExecutionException, InterruptedException;
}