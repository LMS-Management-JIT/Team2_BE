package com.lms.backend.controller;

import com.lms.backend.dto.request.ModuleRequestDto;
import com.lms.backend.dto.response.ModuleResponseDto;
import com.lms.backend.service.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    // Tự động bóc tách Name/UID từ Token đã authenticate qua SecurityConfig
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ModuleResponseDto>> getAllModules() throws ExecutionException, InterruptedException {
        List<ModuleResponseDto> list = moduleService.getAllActiveModules();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponseDto> createModule(@Valid @RequestBody ModuleRequestDto requestDto) throws ExecutionException, InterruptedException {
        String currentUserId = getCurrentUserId();
        ModuleResponseDto savedModule = moduleService.createModule(requestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedModule);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ModuleResponseDto> updateModule(
            @PathVariable String id,
            @Valid @RequestBody ModuleRequestDto requestDto) throws ExecutionException, InterruptedException {
        String currentUserId = getCurrentUserId();
        ModuleResponseDto updatedModule = moduleService.updateModule(id, requestDto, currentUserId);
        return ResponseEntity.ok(updatedModule);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteModule(@PathVariable String id) throws ExecutionException, InterruptedException {
        String currentUserId = getCurrentUserId();
        moduleService.softDeleteModule(id, currentUserId);
        return ResponseEntity.ok("Xóa module thành công!");
    }
}
