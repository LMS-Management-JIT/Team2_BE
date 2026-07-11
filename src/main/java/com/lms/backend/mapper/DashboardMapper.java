package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.response.DashboardCourseProgressResponse;
import com.lms.backend.dto.response.DashboardCourseResponse;
import com.lms.backend.dto.response.DashboardTeacherResponse;
import com.lms.backend.entity.CourseEntity;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper {

    public DashboardCourseResponse toCourseResponse(CourseEntity course) {
        if (course == null) {
            return null;
        }

        return DashboardCourseResponse.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .build();
    }

    public DashboardCourseProgressResponse toCourseProgressResponse(
            CourseEntity course,
            Integer progress,
            Integer totalLessons,
            Integer completedLessons
    ) {
        if (course == null) {
            return null;
        }

        return DashboardCourseProgressResponse.builder()
                .courseId(course.getId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .progress(progress)
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .build();
    }

    public DashboardTeacherResponse toTeacherResponse(DocumentSnapshot teacherDoc, DocumentSnapshot profileDoc) {
        if (teacherDoc == null || !teacherDoc.exists()) {
            return null;
        }

        return DashboardTeacherResponse.builder()
                .id(teacherDoc.getId())
                .profileId(teacherDoc.getString("profile_id"))
                .fullName(profileDoc != null && profileDoc.exists() ? profileDoc.getString("full_name") : null)
                .phoneNumber(profileDoc != null && profileDoc.exists() ? profileDoc.getString("phone_number") : null)
                .gender(profileDoc != null && profileDoc.exists() ? profileDoc.getString("gender") : null)
                .address(profileDoc != null && profileDoc.exists() ? profileDoc.getString("address") : null)
                .specialization(teacherDoc.getString("specialization"))
                .qualification(teacherDoc.getString("qualification"))
                .certifications(teacherDoc.getString("certifications"))
                .available(teacherDoc.getBoolean("available"))
                .build();
    }
}
