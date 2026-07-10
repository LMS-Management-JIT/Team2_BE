package com.lms.backend.service.impl;

import com.google.cloud.firestore.*;
import com.lms.backend.dto.request.DocumentRequest;
import com.lms.backend.dto.response.DocumentResponse;
import com.lms.backend.entity.DocumentEntity;
import com.lms.backend.mapper.DocumentMapper;
import com.lms.backend.repository.DocumentRepository;
import com.lms.backend.service.IDocumentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

@Service
public class DocumentServiceImpl implements IDocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               DocumentMapper documentMapper) {
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
    }

    @Override
    public String createDocument(DocumentRequest request)
            throws ExecutionException, InterruptedException {

        DocumentEntity document = documentMapper.toEntity(request);

        DocumentReference docRef =
                documentRepository.getDocumentCollection().document();

        document.setId(docRef.getId());

        docRef.set(document).get();

        return "Created document with ID: " + docRef.getId();
    }

    @Override
    public DocumentResponse getDocument(String documentId)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot =
                documentRepository.getDocumentCollection()
                        .document(documentId)
                        .get()
                        .get();

        if (!snapshot.exists()) {
            throw new NoSuchElementException("Document not found with ID: " + documentId);
        }

        DocumentEntity entity = documentMapper.toEntity(snapshot);

        if (entity == null) {
            throw new IllegalStateException("Failed to deserialize document: " + documentId);
        }

        return documentMapper.toResponse(entity);
    }

    @Override
    public List<DocumentResponse> searchDocuments()
            throws ExecutionException, InterruptedException {

        QuerySnapshot querySnapshot =
                documentRepository.getDocumentCollection()
                        .get()
                        .get();

        List<DocumentResponse> documents = new ArrayList<>();

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            documents.add(
                    documentMapper.toResponse(
                            documentMapper.toEntity(document)
                    )
            );
        }

        return documents;
    }

    @Override
    public List<DocumentResponse> getDocumentsByModuleId(String moduleId)
            throws ExecutionException, InterruptedException {

        QuerySnapshot querySnapshot =
                documentRepository.getDocumentCollection()
                        .whereEqualTo("moduleId", moduleId)
                        .get()
                        .get();

        List<DocumentResponse> documents = new ArrayList<>();

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            documents.add(
                    documentMapper.toResponse(
                            documentMapper.toEntity(document)
                    )
            );
        }

        return documents;
    }

    @Override
    public String updateDocument(String documentId,
                                 DocumentRequest request)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                documentRepository.getDocumentCollection()
                        .document(documentId);

        return documentRepository.getFirestore().runTransaction(transaction -> {

            DocumentSnapshot snapshot = transaction.get(docRef).get();

            if (!snapshot.exists()) {
                throw new NoSuchElementException("Document not found: " + documentId);
            }

            DocumentEntity document = documentMapper.toEntity(request);
            document.setId(documentId);

            transaction.set(docRef, document, SetOptions.merge());

            return "Updated document with ID: " + documentId;

        }).get();
    }

    @Override
    public String deleteDocument(String documentId)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                documentRepository.getDocumentCollection()
                        .document(documentId);

        return documentRepository.getFirestore().runTransaction(transaction -> {

            DocumentSnapshot snapshot = transaction.get(docRef).get();

            if (!snapshot.exists()) {
                throw new NoSuchElementException("Document not found: " + documentId);
            }

            transaction.delete(docRef);

            return "Deleted document with ID: " + documentId;

        }).get();
    }
}