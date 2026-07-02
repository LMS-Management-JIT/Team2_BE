package com.lms.backend.controller;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassDetailResponse;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.service.IClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ClassController {

    private final IClassService classService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createClass(@Valid @RequestBody ClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Tạo thành công Lớp với ID: " + classService.createClass(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ClassResponse>> getAllClasses() {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ClassDetailResponse> getClassDetail(@PathVariable String id) {
        return ResponseEntity.ok(classService.getClassDetail(id));
    }

    @GetMapping("/{id}/course")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getCourseOfClass(@PathVariable String id) {
        return ResponseEntity.ok(classService.getCourseOfClass(id));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getStudentsOfClass(@PathVariable String id) {
        return ResponseEntity.ok(classService.getStudentsOfClass(id));
    }

    @GetMapping("/{id}/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getTeachersOfClass(@PathVariable String id) {
        return ResponseEntity.ok(classService.getTeachersOfClass(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<String> updateClass(@PathVariable String id, @Valid @RequestBody ClassRequest request) {
        return ResponseEntity.ok(classService.updateClass(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteClass(@PathVariable String id, @RequestParam String deletedBy) {
        return ResponseEntity.ok(classService.deleteClass(id, deletedBy));
    }
}