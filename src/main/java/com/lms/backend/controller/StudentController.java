package com.lms.backend.controller;

import com.lms.backend.dto.request.StudentRequest;
import com.lms.backend.dto.response.StudentResponse;
import com.lms.backend.service.IStudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin("*")
public class StudentController {

    private final IStudentService studentService;

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createStudent(
            @Valid @RequestBody StudentRequest request) throws Exception {

        String id = studentService.createStudent(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Tạo thành công học viên với ID: " + id);
    }

    // READ ALL
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getAllStudents(

            @RequestParam(defaultValue = "1") int page,

            @RequestParam(defaultValue = "7") int size

    ) {

        return ResponseEntity.ok(
                studentService.getAllStudents(page, size)
        );
    }

    // DETAIL
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<StudentResponse> getStudentDetail(
            @PathVariable String id) throws Exception {

        return ResponseEntity.ok(studentService.getStudentDetail(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<String> updateStudent(
            @PathVariable String id,
            @Valid @RequestBody StudentRequest request)
            throws Exception {

        return ResponseEntity.ok(
                studentService.updateStudent(id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteStudent(
            @PathVariable String id
    )
            throws Exception {

        return ResponseEntity.ok(
                studentService.deleteStudent(id)
        );
    }
}