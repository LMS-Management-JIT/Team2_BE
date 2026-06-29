package com.lms.backend.service;

import com.lms.backend.dto.request.ModuleRequestDto;
import com.lms.backend.dto.response.ModuleResponseDto;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ModuleService {
    List<ModuleResponseDto> getAllActiveModules() throws ExecutionException, InterruptedException;
    ModuleResponseDto createModule(ModuleRequestDto requestDto, String creatorId) throws ExecutionException, InterruptedException;
    ModuleResponseDto updateModule(String id, ModuleRequestDto requestDto, String updaterId) throws ExecutionException, InterruptedException;
    void softDeleteModule(String id, String updaterId) throws ExecutionException, InterruptedException;
}