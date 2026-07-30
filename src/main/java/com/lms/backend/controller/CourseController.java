package com.lms.backend.controller;

import com.lms.backend.dto.request.CourseRequest;
import com.lms.backend.dto.request.ReorderModulesRequest;
import com.lms.backend.dto.response.CourseModuleResponse;
import com.lms.backend.dto.response.CourseResponse;
import com.lms.backend.dto.response.ModuleResponse;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.service.ICourseModuleService;
import com.lms.backend.service.ICourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CourseController {

    private final ICourseService courseService;
    private final ICourseModuleService courseModuleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> findAllCourses(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String name) {
        if (page == null && size == null) {
            return ResponseEntity.ok(courseService.findAllCourses());
        }
        int pageVal = (page != null) ? page : 1;
        int sizeVal = (size != null) ? size : 10;
        PagedResponse<CourseResponse> result = courseService.findAllCoursesPage(pageVal, sizeVal, name);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable String id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<String> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Tạo thành công khóa học với ID: " + courseService.createCourse(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable String id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCourse(@PathVariable String id, @RequestParam(required = false) String deletedBy) {
        return ResponseEntity.ok(courseService.deleteCourse(id, deletedBy));
    }

    // ==================== MODULE TRONG COURSE ====================

    @GetMapping("/{courseId}/modules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ModuleResponse>> getModulesByCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(courseModuleService.getModulesDetailByCourseId(courseId));
    }

    @GetMapping("/{courseId}/module-mappings")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<CourseModuleResponse>> getModuleMappingsByCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(courseModuleService.getByCourseId(courseId));
    }

    @PostMapping("/{courseId}/modules/{moduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @SneakyThrows
    public ResponseEntity<CourseModuleResponse> addModuleToCourse(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @RequestParam(required = false) String createdBy) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseModuleService.addModuleToCourse(courseId, moduleId, createdBy));
    }

    @DeleteMapping("/{courseId}/modules/{moduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> removeModuleFromCourse(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @RequestParam(required = false) String updatedBy) {
        courseModuleService.removeModuleFromCourse(courseId, moduleId, updatedBy);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{courseId}/modules/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @SneakyThrows
    public ResponseEntity<Void> reorderModules(
            @PathVariable String courseId,
            @RequestBody ReorderModulesRequest request) {
        request.setCourseId(courseId);
        courseModuleService.reorderModules(courseId, request.getModuleIds(), request.getUpdatedBy());
        return ResponseEntity.ok().build();
    }
}