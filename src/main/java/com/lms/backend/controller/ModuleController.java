package com.lms.backend.controller;

import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;
import com.lms.backend.service.IModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ModuleController {

    private final IModuleService moduleService;

    @PostMapping
    public ResponseEntity<String> createModule(@Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Tạo thành công Module với ID: " + moduleService.createModule(request));
    }

    @GetMapping
    public ResponseEntity<?> getAllModules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam (defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status
    ) {
        if (page == null && size == null) {
            return ResponseEntity.ok(moduleService.getAllModules());
        }
        return ResponseEntity.ok(moduleService.getAllModulesPage(page, size, name, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponse> getModuleById(@PathVariable String id) {
        return ResponseEntity.ok(moduleService.getModuleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateModule(@PathVariable String id, @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.updateModule(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteModule(@PathVariable String id, @RequestParam(required = false) String deletedBy) {
        return ResponseEntity.ok(moduleService.deleteModule(id, deletedBy));
    }
}
