package com.lms.backend.controller;

import com.lms.backend.dto.request.DocumentRequest;
import com.lms.backend.dto.response.DocumentResponse;
import com.lms.backend.service.IDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class DocumentController {

    private final IDocumentService documentService;

    public DocumentController(IDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<String> createDocument(@Valid @RequestBody DocumentRequest request)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createDocument(request));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable String documentId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(documentService.getDocument(documentId));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments()
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(documentService.searchDocuments());
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByModuleId(
            @PathVariable String moduleId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(documentService.getDocumentsByModuleId(moduleId));
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<String> updateDocument(
            @PathVariable String documentId,
            @Valid @RequestBody DocumentRequest request)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(
                documentService.updateDocument(documentId, request));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable String documentId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(
                documentService.deleteDocument(documentId));
    }
}