package com.lms.backend.controller;

import com.lms.backend.dto.request.TopicLessonRequest;
import com.lms.backend.dto.response.TopicLessonResponse;
import com.lms.backend.service.ITopicLessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topic-lessons")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TopicLessonController {

    private final ITopicLessonService topicLessonService;

    @PostMapping
    public ResponseEntity<TopicLessonResponse> create(
            @Valid @RequestBody TopicLessonRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(topicLessonService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TopicLessonResponse>> getAll() {

        return ResponseEntity.ok(
                topicLessonService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicLessonResponse> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                topicLessonService.getById(id)
        );
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<TopicLessonResponse>> getByTopicId(
            @PathVariable String topicId) {

        return ResponseEntity.ok(
                topicLessonService.getByTopicId(topicId)
        );
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<TopicLessonResponse>> getByLessonId(
            @PathVariable String lessonId) {

        return ResponseEntity.ok(
                topicLessonService.getByLessonId(lessonId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TopicLessonResponse> update(
            @PathVariable String id,
            @Valid @RequestBody TopicLessonRequest request) {

        return ResponseEntity.ok(
                topicLessonService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable String id) {

        topicLessonService.delete(id);

        return ResponseEntity.ok("Topic lesson deleted successfully.");
    }
}