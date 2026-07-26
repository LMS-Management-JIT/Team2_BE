package com.lms.backend.controller;

import com.lms.backend.dto.request.AddLessonToTopicRequest;
import com.lms.backend.dto.request.ReorderLessonsRequest;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.dto.response.TopicLessonResponse;
import com.lms.backend.service.ITopicLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/topic-lessons")
@RequiredArgsConstructor
public class TopicLessonController {

    private final ITopicLessonService service;

    @PostMapping("/{topicId}")
    public ResponseEntity<TopicLessonResponse> addLessonToTopic(
            @PathVariable String topicId,
            @RequestBody AddLessonToTopicRequest request) throws ExecutionException, InterruptedException {
        request.setTopicId(topicId);
        TopicLessonResponse response = service.addLessonToTopic(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TopicLessonResponse>> getAllLessonByTopic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String topicId) throws ExecutionException, InterruptedException {

        PagedResponse<TopicLessonResponse> response = service.getAllLessonByTopic(page, size, topicId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{topicId}/{lessonId}")
    public ResponseEntity<Void> removeLessonFromTopic(
            @PathVariable String topicId,
            @PathVariable String lessonId,
            @RequestParam(required = false) String updatedBy) {
        service.removeLessonFromTopic(topicId, lessonId, updatedBy);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{topicId}/reorder")
    public ResponseEntity<Void> reorderLessons(
            @PathVariable String topicId,
            @RequestBody ReorderLessonsRequest request) throws ExecutionException, InterruptedException {
        request.setTopicId(topicId);
        service.reorderLessons(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exists/{lessonId}")
    public ResponseEntity<Boolean> exists(
            @PathVariable String topicId,
            @PathVariable String lessonId) {
        return ResponseEntity.ok(service.exists(topicId, lessonId));
    }
}