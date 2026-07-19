package com.lms.backend.controller;

import com.lms.backend.dto.request.SaveAttendanceRequest;
import com.lms.backend.dto.request.TeacherRequest;
import com.lms.backend.dto.response.TeacherResponse;
import com.lms.backend.dto.response.TeacherScheduleResponse;
import com.lms.backend.dto.response.TeacherStatsResponse;
import com.lms.backend.service.ITeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TeacherController {

    private final ITeacherService teacherService;

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createTeacher(@Valid @RequestBody TeacherRequest request) throws Exception {

        String id = teacherService.createTeacher(request);

        return ResponseEntity.status(HttpStatus.CREATED).body("Tạo thành công giảng viên với ID: " + id);
    }

    // READ ALL (phân trang + filter)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getAllTeachers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String classId, @RequestParam(required = false) Boolean available) {
        return ResponseEntity.ok(teacherService.getAllTeachers(page, size, keyword, classId, available));
    }

    // DETAIL
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<TeacherResponse> getTeacherDetail(@PathVariable String id) throws Exception {

        return ResponseEntity.ok(teacherService.getTeacherDetail(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<String> updateTeacher(@PathVariable String id, @Valid @RequestBody TeacherRequest request) throws Exception {

        return ResponseEntity.ok(teacherService.updateTeacher(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTeacher(@PathVariable String id) throws Exception {

        return ResponseEntity.ok(teacherService.deleteTeacher(id));
    }

    // LẤY TEACHER ID DỰA TRÊN ACCOUNT ID
    @GetMapping("/account/{accountId}/id")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<java.util.Map<String, String>> getTeacherIdByAccountId(
            @PathVariable String accountId) throws Exception {

        String teacherId = teacherService.getTeacherIdByAccountId(accountId);
        return ResponseEntity.ok(java.util.Collections.singletonMap("teacherId", teacherId));
    }

    @GetMapping("/{teacherId}/classes")
    public ResponseEntity<?> getClassesByTeacher(
            @PathVariable String teacherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String program,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(
                teacherService.getClassesByTeacher(teacherId, page, size, name, program, status, startDate, endDate)
        );
    }

    // Lấy thống kê (tổng lớp, tổng học viên)
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<TeacherStatsResponse> getTeacherStats(
            @PathVariable String id) throws Exception {

        return ResponseEntity.ok(
                teacherService.getTeacherStats(id)
        );
    }

    // Lấy danh sách lịch dạy (lấy từ subcollection class_sessions của các lớp)
    @GetMapping("/{id}/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<TeacherScheduleResponse>> getTeacherSchedules(
            @PathVariable String id) throws Exception {

        return ResponseEntity.ok(
                teacherService.getTeacherSchedules(id)
        );
    }

    // Lấy chi tiết từng buổi học của 1 lớp cụ thể (dùng cho modal lịch trình)
    @GetMapping("/classes/{classId}/sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getClassSessions(
            @PathVariable String classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseEntity.ok(teacherService.getClassSessions(classId, page, size));
    }

    // Lấy danh sách điểm danh của 1 buổi học
    @GetMapping("/classes/{classId}/sessions/{sessionId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getSessionAttendance(
            @PathVariable String classId,
            @PathVariable String sessionId) {
        return ResponseEntity.ok(teacherService.getSessionAttendance(classId, sessionId));
    }

    // Lưu điểm danh của 1 buổi học
    @PutMapping("/classes/{classId}/sessions/{sessionId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<String> saveSessionAttendance(
            @PathVariable String classId,
            @PathVariable String sessionId,
            @RequestBody SaveAttendanceRequest request) {
        return ResponseEntity.ok(teacherService.saveSessionAttendance(classId, sessionId, request.getRecords()));
    }
}