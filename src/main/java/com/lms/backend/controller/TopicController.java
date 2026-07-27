package com.lms.backend.controller;

import com.lms.backend.dto.request.TopicRequest;
import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.TopicResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;
import com.lms.backend.service.ITopicService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/topics")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class TopicController {

    private final ITopicService topicService;

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<TopicWithLessonsResponse>> getTopicsByModule(
            @PathVariable String moduleId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(topicService.getTopicsByModule(moduleId));
    }

    @GetMapping("/{topicId}/content")
    public ResponseEntity<List<LessonSummaryResponse>> getLessonsByTopic(
            @PathVariable String topicId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(topicService.getLessonsByTopic(topicId));
    }

/*    @GetMapping("/{topicId}")
    public ResponseEntity<TopicWithLessonsResponse> getTopicDetail(
            @PathVariable String topicId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(topicService.getTopicDetail(topicId));
    }*/
    @GetMapping
    public ResponseEntity<?> getAllTopics(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam (defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status
    ) throws ExecutionException, InterruptedException {
        if (page == null && size == null) {
            return ResponseEntity.ok(topicService.getAllTopics(0,10, "", ""));
        }
        return ResponseEntity.ok(topicService.getAllTopics(page, size, name, status));
    }

    @PostMapping
    public ResponseEntity<TopicResponse> create(@RequestBody TopicRequest request) {
        return new ResponseEntity<>(topicService.create(request), HttpStatus.CREATED);
    }

    @SneakyThrows
    @GetMapping("/{id}")
    public ResponseEntity<TopicResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(topicService.getById(id));
    }

    @SneakyThrows
    @PutMapping("/{id}")
    public ResponseEntity<TopicResponse> update(
            @PathVariable String id,
            @RequestBody TopicRequest request) {
        return ResponseEntity.ok(topicService.update(id, request));
    }

    @SneakyThrows
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestParam(required = false) String deletedBy) {
        topicService.delete(id, deletedBy);
        return ResponseEntity.noContent().build();
    }
}