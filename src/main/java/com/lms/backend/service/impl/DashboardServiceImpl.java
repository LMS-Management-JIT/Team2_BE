package com.lms.backend.service.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.response.*;
import com.lms.backend.entity.CourseEntity;
import com.lms.backend.exception.DashboardException;
//import com.lms.backend.repository.HistoryRepository;
import com.lms.backend.mapper.DashboardMapper;
import com.lms.backend.repository.CourseRepository;
import com.lms.backend.repository.DashboardRepository;
import com.lms.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final IModuleService moduleService;
    private final IClassService classService;
    private final IStudentService studentService;
    private final ITeacherService teacherService;
    private final IHistoryService historyService;
    private final DashboardRepository dashboardRepository;
    private final DashboardMapper dashboardMapper;
    private final CourseRepository courseRepository;

    @Override
    public DashboardResponse getDashboardData() {
        try {
            return DashboardResponse.builder()
                    .totalModules(moduleService.countModules())
                    .totalClasses(classService.countClasses())
                    .totalStudents(studentService.countStudents())
                    .totalTeachers(teacherService.countTeachers())
                    .recentHistories(historyService.getRecentHistories(10))
                    .build();
        } catch (Exception e) {
            throw new DashboardException("Lỗi khi lấy dữ liệu dashboard: " + e.getMessage());
        }
    }
    @Override
    public List<DashboardCourseResponse> getStudentCourses(String studentId) {
        try {
            validateStudent(studentId);

            List<DashboardCourseResponse> responses = new ArrayList<>();
            for (CourseEntity course : getActiveCoursesOfStudent(studentId)) {
                responses.add(dashboardMapper.toCourseResponse(course));
            }
            return responses;
        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy khóa học của học viên", e);
        }
    }

    @Override
    public List<DashboardCourseProgressResponse> getStudentCourseProgress(String studentId) {
        try {
            validateStudent(studentId);

            List<DashboardCourseProgressResponse> responses = new ArrayList<>();
            for (CourseEntity course : getActiveCoursesOfStudent(studentId)) {
                CourseProgressStat stat = calculateCourseProgress(studentId, course.getId());
                responses.add(dashboardMapper.toCourseProgressResponse(
                        course,
                        stat.progress(),
                        stat.totalLessons(),
                        stat.completedLessons()
                ));
            }
            return responses;
        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy tiến độ khóa học", e);
        }
    }

    @Override
    public List<DashboardTeacherResponse> getStudentTeachers(String studentId) {
        try {
            validateStudent(studentId);

            List<String> classIds = dashboardRepository.findClassIdsByStudentId(studentId);
            Set<String> teacherIds = new LinkedHashSet<>();

            for (String classId : classIds) {
                DocumentSnapshot classDoc = dashboardRepository.findClassById(classId);
                if (isDeletedOrNotExist(classDoc)) {
                    continue;
                }
                teacherIds.addAll(dashboardRepository.findTeacherIdsByClassId(classId));
            }

            List<DashboardTeacherResponse> responses = new ArrayList<>();
            for (String teacherId : teacherIds) {
                DocumentSnapshot teacherDoc = dashboardRepository.findTeacherById(teacherId);
                if (isDeletedOrNotExist(teacherDoc)) {
                    continue;
                }

                DocumentSnapshot profileDoc = null;
                String profileId = teacherDoc.getString("profile_id");
                if (profileId != null && !profileId.isBlank()) {
                    profileDoc = dashboardRepository.findProfileById(profileId);
                    if (isDeletedOrNotExist(profileDoc)) {
                        profileDoc = null;
                    }
                }

                DashboardTeacherResponse response = dashboardMapper.toTeacherResponse(teacherDoc, profileDoc);
                if (response != null) {
                    responses.add(response);
                }
            }
            return responses;
        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy danh sách giảng viên", e);
        }
    }
    @Override
    public List<DashboardTeacherResponse> getAllTeachers() {
        try {
            List<DashboardTeacherResponse> responses = new ArrayList<>();

            for (DocumentSnapshot teacherDoc : dashboardRepository.findAllTeachers()) {
                DocumentSnapshot profileDoc = null;

                String profileId = teacherDoc.getString("profile_id");
                if (profileId != null && !profileId.isBlank()) {
                    profileDoc = dashboardRepository.findProfileById(profileId);

                    if (isDeletedOrNotExist(profileDoc)) {
                        profileDoc = null;
                    }
                }

                DashboardTeacherResponse response =
                        dashboardMapper.toTeacherResponse(teacherDoc, profileDoc);

                if (response != null) {
                    responses.add(response);
                }
            }

            return responses;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy danh sách giảng viên", e);
        }
    }
    private void validateStudent(String studentId) throws Exception {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID không được để trống");
        }

        DocumentSnapshot studentDoc = dashboardRepository.findStudentById(studentId);
        if (studentDoc == null || !studentDoc.exists()) {
            throw new NoSuchElementException("Không tìm thấy học viên");
        }

        if (Boolean.TRUE.equals(studentDoc.getBoolean("deleted"))) {
            throw new IllegalArgumentException("Học viên đã bị xóa");
        }

        String status = studentDoc.getString("status");
        if (status != null && !status.isBlank() && !"ACTIVE".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Học viên không ở trạng thái ACTIVE");
        }
    }

    private List<CourseEntity> getActiveCoursesOfStudent(String studentId) throws Exception {
        List<String> classIds = dashboardRepository.findClassIdsByStudentId(studentId);
        Map<String, CourseEntity> courses = new LinkedHashMap<>();

        for (String classId : classIds) {
            DocumentSnapshot classDoc = dashboardRepository.findClassById(classId);
            if (isDeletedOrNotExist(classDoc)) {
                continue;
            }

            String courseId = classDoc.getString("course_id");
            if (courseId == null || courseId.isBlank()) {
                continue;
            }

            CourseEntity course = (CourseEntity) courseRepository.getCourseById(courseId);
            if (course == null || course.isDeleted()) {
                continue;
            }

            courses.put(course.getId(), course);
        }

        return new ArrayList<>(courses.values());
    }

    private CourseProgressStat calculateCourseProgress(String studentId, String courseId) throws Exception {
        Set<String> lessonIds = new LinkedHashSet<>();

        for (String moduleId : dashboardRepository.findModuleIdsByCourseId(courseId)) {
            for (String topicId : dashboardRepository.findTopicIdsByModuleId(moduleId)) {
                lessonIds.addAll(dashboardRepository.findLessonIdsByTopicId(topicId));
            }
        }

        if (lessonIds.isEmpty()) {
            return new CourseProgressStat(0, 0, 0);
        }

        int totalProgress = 0;
        int completedLessons = 0;

        for (String lessonId : lessonIds) {
            DocumentSnapshot progressDoc = dashboardRepository.findLessonProgress(studentId, lessonId);
            if (progressDoc == null || !progressDoc.exists()) {
                continue;
            }

            Long progressValue = progressDoc.getLong("progress");
            int progress = progressValue == null ? 0 : progressValue.intValue();
            totalProgress += Math.min(progress, 100);

            Boolean completed = progressDoc.getBoolean("completed");
            if (Boolean.TRUE.equals(completed) || progress >= 100) {
                completedLessons++;
            }
        }

        int averageProgress = Math.round((float) totalProgress / lessonIds.size());
        return new CourseProgressStat(averageProgress, lessonIds.size(), completedLessons);
    }

    private boolean isDeletedOrNotExist(DocumentSnapshot document) {
        return document == null
                || !document.exists()
                || Boolean.TRUE.equals(document.getBoolean("deleted"));
    }

    private record CourseProgressStat(Integer progress, Integer totalLessons, Integer completedLessons) {
    }
}