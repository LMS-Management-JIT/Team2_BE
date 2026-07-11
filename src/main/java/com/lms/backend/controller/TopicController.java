package com.lms.backend.controller;

import com.lms.backend.dto.response.LessonSummaryResponse;
import com.lms.backend.dto.response.TopicWithLessonsResponse;
import com.lms.backend.service.lTopicService;
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

    private final lTopicService topicService;

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
}