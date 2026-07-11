package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {

    @DocumentId
    private String id;

    @PropertyName("moduleId")
    private String moduleId;

    @PropertyName("documentName")
    private String documentName;

    @PropertyName("type")
    private String type;

    @PropertyName("url")
    private String url;
}