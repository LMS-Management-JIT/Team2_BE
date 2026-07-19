package com.lms.backend.controller;

import com.lms.backend.dto.request.LessonRequest;
import com.lms.backend.dto.response.LessonResponse;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.service.ILessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class LessonController {

    private final ILessonService lessonService;

    @PostMapping
    public ResponseEntity<LessonResponse> createLesson(
            @Valid @RequestBody LessonRequest request) {
        LessonResponse response = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<LessonResponse>> getAllLessons(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status
    ) throws ExecutionException, InterruptedException {
        if (page == null && size == null) {
            return ResponseEntity.ok(lessonService.getAllLessons(0, 10, "", ""));
        }
        return ResponseEntity.ok(lessonService.getAllLessons(page, size, name, status));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(
            @PathVariable String lessonId) {

        return ResponseEntity.ok(
                lessonService.getLessonById(lessonId)
        );
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable String lessonId,
            @Valid @RequestBody LessonRequest request) {

        return ResponseEntity.ok(
                lessonService.updateLesson(lessonId, request)
        );
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<String> deleteLesson(
            @PathVariable String lessonId) {

        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok("Lesson deleted successfully.");
    }
}