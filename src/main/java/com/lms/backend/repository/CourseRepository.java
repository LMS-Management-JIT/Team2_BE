package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class CourseRepository {

    private final Firestore firestore;

    // Lấy danh sách tất cả khóa học chưa bị xóa
    public List<Map<String, Object>> findAllCourses() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> courses = new ArrayList<>();
        QuerySnapshot snapshot = firestore.collection("courses")
                .whereEqualTo("deleted", false)
                .get().get();

        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            courses.add(data);
        }
        return courses;
    }

    // Lấy chi tiết một khóa học theo ID
    public Map<String, Object> getCourseById(String courseId) throws ExecutionException, InterruptedException {
        if (courseId == null || courseId.isEmpty()) return null;
        DocumentSnapshot doc = firestore.collection("courses").document(courseId).get().get();
        if (doc.exists()) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            return data;
        }
        return null;
    }
}