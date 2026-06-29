package com.lms.backend.controller;

import com.lms.backend.dto.request.ModuleRequestDto;
import com.lms.backend.dto.response.ModuleResponseDto;
import com.lms.backend.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/modules")
@CrossOrigin(origins = "*")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @GetMapping
    public ResponseEntity<List<ModuleResponseDto>> getAllModules() throws ExecutionException, InterruptedException {
        List<ModuleResponseDto> list = moduleService.getAllActiveModules();
        return new ResponseEntity<>(list, HttpStatus.OK); // Trả về 200 OK
    }

    @PostMapping
    public ResponseEntity<ModuleResponseDto> createModule(@Valid @RequestBody ModuleRequestDto requestDto) throws ExecutionException, InterruptedException {
        String currentUserId = "teacher_lms_01";
        ModuleResponseDto savedModule = moduleService.createModule(requestDto, currentUserId);
        return new ResponseEntity<>(savedModule, HttpStatus.CREATED); // Trả về 201 Created
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleResponseDto> updateModule(@PathVariable String id, @Valid @RequestBody ModuleRequestDto requestDto) throws ExecutionException, InterruptedException {
        String currentUserId = "teacher_lms_01";
        ModuleResponseDto updatedModule = moduleService.updateModule(id, requestDto, currentUserId);
        return new ResponseEntity<>(updatedModule, HttpStatus.OK); // Trả về 200 OK
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteModule(@PathVariable String id) throws ExecutionException, InterruptedException {
        String currentUserId = "admin_lms_01";
        moduleService.softDeleteModule(id, currentUserId);
        return new ResponseEntity<>("Xóa học phần thành công!", HttpStatus.OK); // Trả về 200 OK
    }
}