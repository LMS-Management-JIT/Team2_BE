package com.lms.backend.controller;

import com.lms.backend.dto.request.LessonProgressRequest;
import com.lms.backend.dto.response.LessonProgressResponse;
import com.lms.backend.service.ILessonProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lesson-progress")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class LessonProgressController {

    private final ILessonProgressService lessonProgressService;

    @PostMapping
    public LessonProgressResponse create(
            @Valid @RequestBody LessonProgressRequest request) {

        return lessonProgressService.createLessonProgress(request);
    }

    @GetMapping("/{id}")
    public LessonProgressResponse getById(
            @PathVariable String id) {

        return lessonProgressService.getLessonProgressById(id);
    }

    @GetMapping
    public List<LessonProgressResponse> getAll() {

        return lessonProgressService.getAllLessonProgress();
    }

    @GetMapping("/student/{studentId}")
    public List<LessonProgressResponse> getByStudent(
            @PathVariable String studentId) {

        return lessonProgressService.getByStudentId(studentId);
    }

    @PutMapping("/{id}")
    public LessonProgressResponse update(
            @PathVariable String id,
            @Valid @RequestBody LessonProgressRequest request) {
        return lessonProgressService.updateLessonProgress(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable String id) {
        lessonProgressService.deleteLessonProgress(id);
    }
}