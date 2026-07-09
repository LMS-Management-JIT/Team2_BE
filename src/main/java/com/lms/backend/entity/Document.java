package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @PropertyName("module_id")
    private String moduleId;

    @PropertyName("document_name")
    private String documentName;

    @PropertyName("type")
    private String type;

    @PropertyName("url")
    private String url;
}