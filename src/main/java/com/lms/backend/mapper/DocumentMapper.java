package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.DocumentRequest;
import com.lms.backend.dto.response.DocumentResponse;
import com.lms.backend.entity.DocumentEntity;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentEntity toEntity(DocumentRequest request) {

        return DocumentEntity.builder()
                .moduleId(request.getModuleId())
                .documentName(request.getDocumentName())
                .type(request.getType())
                .url(request.getUrl())
                .build();
    }

    public DocumentEntity toEntity(DocumentSnapshot document) {

        if (document == null) {
            return null;
        }
        
        if (!document.exists()) {
            System.err.println("Document does not exist: " + document.getId());
            return null;
        }

        try {
            DocumentEntity entity = document.toObject(DocumentEntity.class);
            if (entity == null) {
                System.err.println("Failed to deserialize document " + document.getId() + ". Data: " + document.getData());
                return null;
            }
            entity.setId(document.getId());
            return entity;
        } catch (Exception e) {
            System.err.println("Error deserializing document " + document.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public DocumentResponse toResponse(DocumentEntity entity) {

        if (entity == null) {
            return null;
        }

        return DocumentResponse.builder()
                .id(entity.getId())
                .moduleId(entity.getModuleId())
                .documentName(entity.getDocumentName())
                .type(entity.getType())
                .url(entity.getUrl())
                .build();
    }
}