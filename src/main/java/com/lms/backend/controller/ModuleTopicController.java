package com.lms.backend.controller;

import com.lms.backend.dto.request.ModuleTopicRequest;
import com.lms.backend.dto.response.ModuleTopicResponse;
import com.lms.backend.service.IModuleTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/module-topics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ModuleTopicController {

    private final IModuleTopicService moduleTopicService;

    @PostMapping
    public ResponseEntity<ModuleTopicResponse> create(
            @Valid @RequestBody ModuleTopicRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(moduleTopicService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ModuleTopicResponse>> getAll() {

        return ResponseEntity.ok(
                moduleTopicService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleTopicResponse> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                moduleTopicService.getById(id)
        );
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ModuleTopicResponse>> getByModuleId(
            @PathVariable String moduleId) {

        return ResponseEntity.ok(
                moduleTopicService.getByModuleId(moduleId)
        );
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<ModuleTopicResponse>> getByTopicId(
            @PathVariable String topicId) {

        return ResponseEntity.ok(
                moduleTopicService.getByTopicId(topicId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleTopicResponse> update(
            @PathVariable String id,
            @Valid @RequestBody ModuleTopicRequest request) {

        return ResponseEntity.ok(
                moduleTopicService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable String id) {

        moduleTopicService.delete(id);

        return ResponseEntity.ok("Module topic relation deleted successfully.");
    }
}