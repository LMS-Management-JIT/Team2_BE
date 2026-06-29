package com.lms.backend.controller;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.service.ClassService;
import com.lms.backend.service.IClassService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    @Autowired
    private IClassService classService;

    // @Valid: Kích hoạt kiểm tra dữ liệu từ ClassRequest
    // @PreAuthorize: Chỉ User có role ADMIN mới được tạo lớp
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createClass(@Valid @RequestBody ClassRequest request) {
        try {
            String id = classService.createClass(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Tạo thành công Lớp với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống khi tạo lớp: " + e.getMessage());
        }
    }

    // Ai cũng có thể xem danh sách lớp (miễn là đã đăng nhập)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getAllClasses() {
        try {
            List<ClassResponse> classes = classService.getAllClasses();
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách lớp: " + e.getMessage());
        }
    }

    // Chỉ ADMIN hoặc TEACHER mới được cập nhật thông tin lớp
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> updateClass(@PathVariable String id, @Valid @RequestBody ClassRequest request) {
        try {
            String result = classService.updateClass(id, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Trả về lỗi 400 (Bad Request) nếu lớp không tồn tại như đã set trong Service
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Chỉ ADMIN mới có quyền xóa lớp
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClass(@PathVariable String id, @RequestParam String deletedBy) {
        try {
            String result = classService.deleteClass(id, deletedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Trả về lỗi 400 (Bad Request) nếu lớp không tồn tại
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}