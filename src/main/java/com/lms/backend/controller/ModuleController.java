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

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ModuleController {

    private final IModuleService moduleService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ModuleResponse>> getAllModules() {
        return ResponseEntity.ok(moduleService.getAllActiveModules());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> createModule(@Valid @RequestBody ModuleRequest request) {
        String currentUserId = getCurrentUserId();
        ModuleResponse savedModule = moduleService.createModule(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedModule);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable String id,
            @Valid @RequestBody ModuleRequest request) {
        String currentUserId = getCurrentUserId();
        ModuleResponse updatedModule = moduleService.updateModule(id, request, currentUserId);
        return ResponseEntity.ok(updatedModule);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteModule(@PathVariable String id) {
        String currentUserId = getCurrentUserId();
        moduleService.softDeleteModule(id, currentUserId);
        return ResponseEntity.ok("Xóa module thành công!");
    }
}