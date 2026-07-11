package com.lms.backend.controller;

import com.lms.backend.dto.request.CourseModuleRequest;
import com.lms.backend.dto.response.CourseModuleResponse;
import com.lms.backend.service.ICourseModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-modules")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CourseModuleController {

    private final ICourseModuleService courseModuleService;

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody CourseModuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Tạo thành công CourseModule với ID: " + courseModuleService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CourseModuleResponse>> getAll() {
        return ResponseEntity.ok(courseModuleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseModuleResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(courseModuleService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable String id,
            @Valid @RequestBody CourseModuleRequest request) {
        return ResponseEntity.ok(courseModuleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id, @RequestParam(required = false) String deletedBy) {
        return ResponseEntity.ok(courseModuleService.delete(id, deletedBy));
    }
    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<List<com.lms.backend.dto.response.ModuleResponse>> getModulesByCourseId(@PathVariable String courseId) {
        return ResponseEntity.ok(courseModuleService.getModulesDetailByCourseId(courseId));
    }
}