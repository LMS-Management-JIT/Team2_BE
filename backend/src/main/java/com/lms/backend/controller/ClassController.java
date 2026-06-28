package com.lms.backend.controller;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.service.ClassService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    @Autowired
    private ClassService classService;

    // @Valid: Kích hoạt kiểm tra dữ liệu từ ClassRequest
//     @PreAuthorize: Chỉ User có role ADMIN mới được tạo lớp
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createClass(@Valid @RequestBody ClassRequest request) throws ExecutionException, InterruptedException {
        String id = classService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Tạo thành công Lớp với ID: " + id);
    }

    // Ai cũng có thể xem danh sách lớp (miễn là đã đăng nhập)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ClassResponse>> getAllClasses() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    // Chỉ ADMIN hoặc TEACHER mới được cập nhật thông tin lớp
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> updateClass(@PathVariable String id, @Valid @RequestBody ClassRequest request) throws ExecutionException, InterruptedException {
        String result = classService.updateClass(id, request);
        return ResponseEntity.ok(result);
    }

    // Chỉ ADMIN mới có quyền xóa lớp
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClass(@PathVariable String id, @RequestParam String deletedBy) throws ExecutionException, InterruptedException {
        String result = classService.deleteClass(id, deletedBy);
        return ResponseEntity.ok(result);
    }
}