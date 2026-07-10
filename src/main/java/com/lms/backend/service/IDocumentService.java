package com.lms.backend.service;

import com.lms.backend.dto.request.DocumentRequest;
import com.lms.backend.dto.response.DocumentResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IDocumentService {

    String createDocument(DocumentRequest request)
            throws ExecutionException, InterruptedException;

    DocumentResponse getDocument(String documentId)
            throws ExecutionException, InterruptedException;

    List<DocumentResponse> searchDocuments()
            throws ExecutionException, InterruptedException;

    List<DocumentResponse> getDocumentsByModuleId(String moduleId)
            throws ExecutionException, InterruptedException;

    String updateDocument(String documentId, DocumentRequest request)
            throws ExecutionException, InterruptedException;

    String deleteDocument(String documentId)
            throws ExecutionException, InterruptedException;
}