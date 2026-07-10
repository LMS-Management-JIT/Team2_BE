package com.lms.backend.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final Firestore firestore;

    public DocumentSnapshot findStudentById(String studentId) throws ExecutionException, InterruptedException {
        return firestore.collection("students").document(studentId).get().get();
    }

    public DocumentSnapshot findClassById(String classId) throws ExecutionException, InterruptedException {
        return firestore.collection("classes").document(classId).get().get();
    }

    public DocumentSnapshot findTeacherById(String teacherId) throws ExecutionException, InterruptedException {
        return firestore.collection("teachers").document(teacherId).get().get();
    }

    public DocumentSnapshot findProfileById(String profileId) throws ExecutionException, InterruptedException {
        return firestore.collection("profiles").document(profileId).get().get();
    }

    public List<String> findClassIdsByStudentId(String studentId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> relations = firestore.collection("student_class")
                .whereEqualTo("student_id", studentId)
                .whereEqualTo("deleted", false)
                .get()
                .get()
                .getDocuments();

        Set<String> classIds = new LinkedHashSet<>();
        for (QueryDocumentSnapshot relation : relations) {
            String classId = relation.getString("class_id");
            if (classId != null && !classId.isBlank()) {
                classIds.add(classId);
            }
        }
        return new ArrayList<>(classIds);
    }

    public List<String> findTeacherIdsByClassId(String classId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> relations = firestore.collection("teacher_class")
                .whereEqualTo("class_id", classId)
                .whereEqualTo("deleted", false)
                .get()
                .get()
                .getDocuments();

        Set<String> teacherIds = new LinkedHashSet<>();
        for (QueryDocumentSnapshot relation : relations) {
            String teacherId = relation.getString("teacher_id");
            if (teacherId != null && !teacherId.isBlank()) {
                teacherIds.add(teacherId);
            }
        }
        return new ArrayList<>(teacherIds);
    }

    public List<String> findModuleIdsByCourseId(String courseId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = firestore.collection("course_modules")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("deleted", false)
                .get()
                .get()
                .getDocuments();

        Set<String> moduleIds = new LinkedHashSet<>();
        for (QueryDocumentSnapshot document : documents) {
            String moduleId = document.getString("moduleId");
            if (moduleId != null && !moduleId.isBlank()) {
                moduleIds.add(moduleId);
            }
        }
        return new ArrayList<>(moduleIds);
    }

    public List<String> findTopicIdsByModuleId(String moduleId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = firestore.collection("topics")
                .whereEqualTo("module_id", moduleId)
                .whereEqualTo("deleted", false)
                .get()
                .get()
                .getDocuments();

        Set<String> topicIds = new LinkedHashSet<>();
        for (QueryDocumentSnapshot document : documents) {
            topicIds.add(document.getId());
        }
        return new ArrayList<>(topicIds);
    }

    public List<String> findLessonIdsByTopicId(String topicId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = firestore.collection("topic_lessons")
                .whereEqualTo("topic_id", topicId)
                .whereEqualTo("deleted", false)
                .get()
                .get()
                .getDocuments();

        Set<String> lessonIds = new LinkedHashSet<>();
        for (QueryDocumentSnapshot document : documents) {
            String lessonId = document.getString("lesson_id");
            if (lessonId != null && !lessonId.isBlank()) {
                lessonIds.add(lessonId);
            }
        }
        return new ArrayList<>(lessonIds);
    }

    public DocumentSnapshot findLessonProgress(String studentId, String lessonId)
            throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = firestore.collection("lesson_progress")
                .whereEqualTo("student_id", studentId)
                .whereEqualTo("lesson_id", lessonId)
                .limit(1)
                .get()
                .get()
                .getDocuments();

        if (documents.isEmpty()) {
            return null;
        }
        return documents.get(0);
    }
    public List<QueryDocumentSnapshot> findAllTeachers() throws ExecutionException, InterruptedException {
        return firestore.collection("teachers")
                .whereEqualTo("deleted", false)
                .get()
                .get()
                .getDocuments();
    }
}
