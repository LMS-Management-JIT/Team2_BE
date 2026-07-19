package com.lms.backend.controller;

import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;
import com.lms.backend.service.ITopicService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{topicId}/lessons")
    public ResponseEntity<List<LessonSummaryResponse>> getLessonsByTopic(
            @PathVariable String topicId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(topicService.getLessonsByTopic(topicId));
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<TopicWithLessonsResponse> getTopicDetail(
            @PathVariable String topicId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(topicService.getTopicDetail(topicId));
    }
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
}