package com.lms.backend.controller;

import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;
import com.lms.backend.service.IModuleService;
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

    private final IModuleService moduleService;

    // Tự động bóc tách Name/UID từ Token đã authenticate qua SecurityConfig
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    //@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ModuleResponse>> getAllModules() throws ExecutionException, InterruptedException {
        List<ModuleResponse> list = moduleService.getAllActiveModules();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> createModule(@Valid @RequestBody ModuleRequest request) throws ExecutionException, InterruptedException {
        String currentUserId = getCurrentUserId();
        ModuleResponse savedModule = moduleService.createModule(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedModule);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable String id,
            @Valid @RequestBody ModuleRequest request) throws ExecutionException, InterruptedException {
        String currentUserId = getCurrentUserId();
        ModuleResponse updatedModule = moduleService.updateModule(id, request, currentUserId);
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
