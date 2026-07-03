package com.lms.backend.controller;

import com.lms.backend.dto.request.TeacherRequest;
import com.lms.backend.dto.response.TeacherResponse;
import com.lms.backend.service.ITeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TeacherController {

    private final ITeacherService teacherService;

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createTeacher(
            @Valid @RequestBody TeacherRequest request) throws Exception {

        String id = teacherService.createTeacher(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Tạo thành công giảng viên với ID: " + id);
    }

    // READ ALL
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<TeacherResponse>> getAllTeachers()
            throws Exception {

        return ResponseEntity.ok(
                teacherService.getAllTeachers()
        );
    }

    // DETAIL
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<TeacherResponse> getTeacherDetail(
            @PathVariable String id) throws Exception {

        return ResponseEntity.ok(
                teacherService.getTeacherDetail(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<String> updateTeacher(
            @PathVariable String id,
            @Valid @RequestBody TeacherRequest request)
            throws Exception {

        return ResponseEntity.ok(
                teacherService.updateTeacher(id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTeacher(
            @PathVariable String id,
            @RequestParam String deletedBy)
            throws Exception {

        return ResponseEntity.ok(
                teacherService.deleteTeacher(id, deletedBy)
        );
    }
}