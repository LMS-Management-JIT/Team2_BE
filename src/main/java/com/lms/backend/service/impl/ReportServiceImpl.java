package com.lms.backend.service.impl;

import com.lms.backend.dto.response.AbsenceRateReportResponse;
import com.lms.backend.dto.response.ClassProgressReportResponse;
import com.lms.backend.dto.response.NewStudentReportResponse;
import com.lms.backend.dto.response.ReportDashboardResponse;
import com.lms.backend.entity.AttendanceEntity;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.entity.ClassSessionEntity;
import com.lms.backend.entity.ProfileEntity;
import com.lms.backend.entity.StudentClassEntity;
import com.lms.backend.entity.StudentEntity;
import com.lms.backend.entity.CourseModuleEntity;
import com.lms.backend.entity.ModuleTopicEntity;
import com.lms.backend.entity.TopicLessonEntity;
import com.lms.backend.entity.LessonProgressEntity;
import com.lms.backend.repository.CourseModuleRepository;
import com.lms.backend.repository.ModuleTopicRepository;
import com.lms.backend.repository.TopicLessonRepository;
import com.lms.backend.repository.LessonProgressRepository;
import com.lms.backend.exception.BadRequestException;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.repository.AttendanceRepository;
import com.lms.backend.repository.ClassRepository;
import com.lms.backend.repository.ClassSessionRepository;
import com.lms.backend.repository.ProfileRepository;
import com.lms.backend.repository.StudentClassRepository;
import com.lms.backend.repository.StudentRepository;
import com.lms.backend.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.lms.backend.dto.response.ClassAbsenceReportResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("d/M");
    private static final Set<String> VALID_GROUP_BY = Set.of("WEEK", "MONTH", "YEAR");
    private static final Set<String> ABSENT_STATUSES = Set.of("Vắng", "Vắng phép");

    private final StudentRepository studentRepository;
    private final StudentClassRepository studentClassRepository;
    private final ClassRepository classRepository;
    private final ProfileRepository profileRepository;
    private final ClassSessionRepository classSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final ModuleTopicRepository moduleTopicRepository;
    private final TopicLessonRepository topicLessonRepository;
    private final LessonProgressRepository lessonProgressRepository;

    // ================== CHART 1: TỶ LỆ NGHỈ HỌC (dữ liệu thật từ class_sessions + attendances) ==================
    @Override
    public AbsenceRateReportResponse getAbsenceRateReport(LocalDate from, LocalDate to, String groupBy) {
        if (from == null || to == null || groupBy == null) {
            throw new BadRequestException("from, to, groupBy không được để trống");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("'to' không được đứng trước 'from'");
        }
        String normalizedGroupBy = groupBy.trim().toUpperCase();
        if (!VALID_GROUP_BY.contains(normalizedGroupBy)) {
            throw new BadRequestException("groupBy chỉ chấp nhận: WEEK, MONTH, YEAR");
        }

        try {
            long totalDays = ChronoUnit.DAYS.between(from, to) + 1;
            LocalDate previousTo = from.minusDays(1);
            LocalDate previousFrom = previousTo.minusDays(totalDays - 1);

            List<ClassSessionEntity> currentSessions = classSessionRepository.findInDateRange(from.toString(), to.toString());
            List<ClassSessionEntity> previousSessions = classSessionRepository.findInDateRange(previousFrom.toString(), previousTo.toString());

            List<AbsenceRateReportResponse.Point> current = buildBuckets(from, to, normalizedGroupBy, currentSessions);
            List<AbsenceRateReportResponse.Point> previous = buildBuckets(previousFrom, previousTo, normalizedGroupBy, previousSessions);

            double avgCurrent = average(current);
            double avgPrevious = average(previous);

            AbsenceRateReportResponse.Point peak = current.stream()
                    .max((a, b) -> Double.compare(a.getAbsenceRate(), b.getAbsenceRate()))
                    .orElse(null);

            int totalAbsences = countTotalAbsences(currentSessions);

            return AbsenceRateReportResponse.builder()
                    .groupBy(normalizedGroupBy)
                    .currentPeriod(current)
                    .previousPeriod(previous)
                    .averageCurrentPeriod(round1(avgCurrent))
                    .averagePreviousPeriod(round1(avgPrevious))
                    .improvementPercent(round1(avgPrevious - avgCurrent))
                    .peakAbsenceLabel(peak != null ? peak.getPeriodLabel() + " - " + peak.getDateRangeLabel() : null)
                    .totalAbsences(totalAbsences)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy báo cáo tỷ lệ nghỉ học: " + e.getMessage(), e);
        }
    }

    /**
     * Chia [from, to] thành các bucket theo groupBy, mỗi bucket tính tỷ lệ nghỉ học thật
     * dựa trên các buổi học (ClassSessionEntity) rơi vào khoảng đó.
     */
    private List<AbsenceRateReportResponse.Point> buildBuckets(
            LocalDate from, LocalDate to, String groupBy, List<ClassSessionEntity> sessions) throws Exception {

        List<AbsenceRateReportResponse.Point> points = new ArrayList<>();
        int index = 1;

        switch (groupBy) {
            case "WEEK" -> {
                LocalDate cursor = from;
                while (!cursor.isAfter(to)) {
                    LocalDate bucketEnd = cursor.plusDays(6);
                    if (bucketEnd.isAfter(to)) {
                        bucketEnd = to;
                    }
                    points.add(buildPoint(
                            "Tuần " + index,
                            cursor.format(DATE_LABEL) + "-" + bucketEnd.format(DATE_LABEL),
                            sessionsInRange(sessions, cursor, bucketEnd)));
                    cursor = cursor.plusDays(7);
                    index++;
                }
            }
            case "MONTH" -> {
                YearMonth cursor = YearMonth.from(from);
                YearMonth end = YearMonth.from(to);
                while (!cursor.isAfter(end)) {
                    points.add(buildPoint(
                            "Tháng " + cursor.getMonthValue(),
                            cursor.getMonthValue() + "/" + cursor.getYear(),
                            sessionsInRange(sessions, cursor.atDay(1), cursor.atEndOfMonth())));
                    cursor = cursor.plusMonths(1);
                    index++;
                }
            }
            case "YEAR" -> {
                int cursorYear = from.getYear();
                int endYear = to.getYear();
                while (cursorYear <= endYear) {
                    LocalDate yearStart = LocalDate.of(cursorYear, 1, 1);
                    LocalDate yearEnd = LocalDate.of(cursorYear, 12, 31);
                    points.add(buildPoint(
                            "Năm " + cursorYear,
                            String.valueOf(cursorYear),
                            sessionsInRange(sessions, yearStart, yearEnd)));
                    cursorYear++;
                    index++;
                }
            }
        }
        return points;
    }

    private List<ClassSessionEntity> sessionsInRange(List<ClassSessionEntity> sessions, LocalDate start, LocalDate end) {
        return sessions.stream()
                .filter(s -> s.getSessionDate() != null)
                .filter(s -> {
                    LocalDate date = LocalDate.parse(s.getSessionDate());
                    return !date.isBefore(start) && !date.isAfter(end);
                })
                .toList();
    }

    private AbsenceRateReportResponse.Point buildPoint(String label, String dateRangeLabel, List<ClassSessionEntity> bucketSessions) throws Exception {
        int totalRecords = 0;
        int absentRecords = 0;

        for (ClassSessionEntity session : bucketSessions) {
            List<AttendanceEntity> attendances = attendanceRepository.findBySession(session.getClassId(), session.getId());
            totalRecords += attendances.size();
            absentRecords += (int) attendances.stream()
                    .filter(a -> ABSENT_STATUSES.contains(a.getStatus()))
                    .count();
        }

        double rate = totalRecords == 0 ? 0 : round1(((double) absentRecords / totalRecords) * 100);

        return AbsenceRateReportResponse.Point.builder()
                .periodLabel(label)
                .dateRangeLabel(dateRangeLabel)
                .absenceRate(rate)
                .totalSessions(bucketSessions.size())
                .build();
    }

    private int countTotalAbsences(List<ClassSessionEntity> sessions) throws Exception {
        int total = 0;
        for (ClassSessionEntity session : sessions) {
            List<AttendanceEntity> attendances = attendanceRepository.findBySession(session.getClassId(), session.getId());
            total += (int) attendances.stream().filter(a -> ABSENT_STATUSES.contains(a.getStatus())).count();
        }
        return total;
    }

    private double average(List<AbsenceRateReportResponse.Point> points) {
        return points.stream().mapToDouble(AbsenceRateReportResponse.Point::getAbsenceRate).average().orElse(0);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    // ================== CHART 2: HỌC SINH ĐĂNG KÝ MỚI (dữ liệu thật từ students.create_date) ==================
    private static final Set<String> VALID_PERIOD_TYPE = Set.of("MONTH", "YEAR");
    private static final Set<String> VALID_COMPARE_MODE = Set.of("PREVIOUS", "CUSTOM");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MM/yyyy");

    @Override
    public NewStudentReportResponse getNewStudentReport(
            String periodType, String current, String compareMode, String comparePeriod) {
        String normalizedPeriodType = periodType == null || periodType.isBlank()
                ? "MONTH" : periodType.trim().toUpperCase();
        if (!VALID_PERIOD_TYPE.contains(normalizedPeriodType)) {
            throw new BadRequestException("periodType chỉ chấp nhận: MONTH, YEAR");
        }

        String normalizedCompareMode = compareMode == null || compareMode.isBlank()
                ? "PREVIOUS" : compareMode.trim().toUpperCase();
        if (!VALID_COMPARE_MODE.contains(normalizedCompareMode)) {
            throw new BadRequestException("compareMode chỉ chấp nhận: PREVIOUS, CUSTOM");
        }
        if ("CUSTOM".equals(normalizedCompareMode) && (comparePeriod == null || comparePeriod.isBlank())) {
            throw new BadRequestException("comparePeriod không được để trống khi compareMode = CUSTOM");
        }

        try {
            List<StudentEntity> allStudents = studentRepository.findAllActive();

            if ("YEAR".equals(normalizedPeriodType)) {
                int currentYear = current == null || current.isBlank()
                        ? LocalDate.now().getYear() : Integer.parseInt(current.trim());
                int compareYear = "CUSTOM".equals(normalizedCompareMode)
                        ? Integer.parseInt(comparePeriod.trim())
                        : currentYear - 1;

                if (compareYear >= currentYear) {
                    throw new BadRequestException("comparePeriod phải là năm trước năm hiện tại");
                }

                List<NewStudentReportResponse.BucketCount> currentBuckets = buildYearBuckets(allStudents, currentYear);
                List<NewStudentReportResponse.BucketCount> compareBuckets = buildYearBuckets(allStudents, compareYear);

                int currentTotal = currentBuckets.stream().mapToInt(NewStudentReportResponse.BucketCount::getCount).sum();
                int compareTotal = compareBuckets.stream().mapToInt(NewStudentReportResponse.BucketCount::getCount).sum();

                return NewStudentReportResponse.builder()
                        .periodType(normalizedPeriodType)
                        .currentLabel(String.valueOf(currentYear))
                        .compareLabel(String.valueOf(compareYear))
                        .currentBuckets(currentBuckets)
                        .compareBuckets(compareBuckets)
                        .currentTotal(currentTotal)
                        .compareTotal(compareTotal)
                        .growthPercent(growthPercent(currentTotal, compareTotal))
                        .build();
            }

            YearMonth currentMonth = current == null || current.isBlank()
                    ? YearMonth.now() : YearMonth.parse(current.trim());
            YearMonth compareYearMonth = "CUSTOM".equals(normalizedCompareMode)
                    ? YearMonth.parse(comparePeriod.trim())
                    : currentMonth.minusMonths(1);

            if (!compareYearMonth.isBefore(currentMonth)) {
                throw new BadRequestException("comparePeriod phải là tháng trước tháng hiện tại");
            }

            List<NewStudentReportResponse.BucketCount> currentBuckets = buildMonthWeekBuckets(allStudents, currentMonth);
            List<NewStudentReportResponse.BucketCount> compareBuckets = buildMonthWeekBuckets(allStudents, compareYearMonth);

            int currentTotal = currentBuckets.stream().mapToInt(NewStudentReportResponse.BucketCount::getCount).sum();
            int compareTotal = compareBuckets.stream().mapToInt(NewStudentReportResponse.BucketCount::getCount).sum();

            return NewStudentReportResponse.builder()
                    .periodType(normalizedPeriodType)
                    .currentLabel(currentMonth.format(MONTH_LABEL))
                    .compareLabel(compareYearMonth.format(MONTH_LABEL))
                    .currentBuckets(currentBuckets)
                    .compareBuckets(compareBuckets)
                    .currentTotal(currentTotal)
                    .compareTotal(compareTotal)
                    .growthPercent(growthPercent(currentTotal, compareTotal))
                    .build();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy báo cáo học sinh đăng ký mới: " + e.getMessage(), e);
        }
    }

    private List<NewStudentReportResponse.BucketCount> buildMonthWeekBuckets(List<StudentEntity> allStudents, YearMonth month) {
        List<NewStudentReportResponse.BucketCount> weeks = new ArrayList<>();
        LocalDate firstDay = month.atDay(1);
        LocalDate lastDay = month.atEndOfMonth();

        LocalDate weekStart = firstDay;
        int weekIndex = 1;
        while (!weekStart.isAfter(lastDay)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(lastDay)) {
                weekEnd = lastDay;
            }

            LocalDate finalWeekStart = weekStart;
            LocalDate finalWeekEnd = weekEnd;
            int count = (int) allStudents.stream()
                    .filter(s -> s.getCreateDate() != null)
                    .filter(s -> {
                        LocalDate created = s.getCreateDate().toLocalDate();
                        return !created.isBefore(finalWeekStart) && !created.isAfter(finalWeekEnd);
                    })
                    .count();

            weeks.add(NewStudentReportResponse.BucketCount.builder()
                    .bucketLabel("Tuần " + weekIndex)
                    .count(count)
                    .build());

            weekStart = weekStart.plusDays(7);
            weekIndex++;
        }
        return weeks;
    }

    private List<NewStudentReportResponse.BucketCount> buildYearBuckets(List<StudentEntity> allStudents, int year) {
        List<NewStudentReportResponse.BucketCount> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            int count = (int) allStudents.stream()
                    .filter(s -> s.getCreateDate() != null)
                    .filter(s -> YearMonth.from(s.getCreateDate()).equals(ym))
                    .count();
            months.add(NewStudentReportResponse.BucketCount.builder()
                    .bucketLabel("Tháng " + m)
                    .count(count)
                    .build());
        }
        return months;
    }

    private double growthPercent(int currentTotal, int compareTotal) {
        return compareTotal == 0
                ? (currentTotal > 0 ? 100.0 : 0.0)
                : round1(((double) (currentTotal - compareTotal) / compareTotal) * 100);
    }

    // ================== CHART 3: TIẾN ĐỘ HOÀN THÀNH KHÓA HỌC (dữ liệu thật từ lesson_progress) ==================
    @Override
    public ClassProgressReportResponse getClassProgressReport(String classId) {
        try {
            ClassEntity classEntity = classRepository.findById(classId);
            if (classEntity == null || classEntity.isDeleted()) {
                throw new ResourceNotFoundException("Lớp học không tồn tại hoặc đã bị xóa!");
            }

            String courseId = classEntity.getCourseId();
            Set<String> lessonIds = collectLessonIdsOfCourse(courseId);

            List<StudentClassEntity> relations = studentClassRepository.findByClassId(classId);

            List<ClassProgressReportResponse.StudentProgress> studentProgresses = new ArrayList<>();
            int good = 0, needsAttention = 0, perfect = 0;

            for (StudentClassEntity relation : relations) {
                StudentEntity student = studentRepository.findById(relation.getStudentId());
                if (student == null || Boolean.TRUE.equals(student.getDeleted())) {
                    continue;
                }

                String fullName = resolveFullName(student);
                int progress = lessonIds.isEmpty() ? 0 : calculateAverageProgress(student.getId(), lessonIds);

                if (progress == 100) {
                    perfect++;
                }
                if (progress >= 80) {
                    good++;
                } else if (progress < 50) {
                    needsAttention++;
                }

                studentProgresses.add(ClassProgressReportResponse.StudentProgress.builder()
                        .studentId(student.getId())
                        .fullName(fullName)
                        .progressPercent(progress)
                        .build());
            }

            return ClassProgressReportResponse.builder()
                    .classId(classId)
                    .className(classEntity.getClassName())
                    .totalStudents(studentProgresses.size())
                    .goodCount(good)
                    .needsAttentionCount(needsAttention)
                    .perfectCount(perfect)
                    .students(studentProgresses)
                    .build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy báo cáo tiến độ lớp học: " + e.getMessage(), e);
        }
    }

    private Set<String> collectLessonIdsOfCourse(String courseId) throws Exception {
        Set<String> lessonIds = new LinkedHashSet<>();
        if (courseId == null || courseId.isBlank()) {
            return lessonIds;
        }

        List<CourseModuleEntity> courseModules = courseModuleRepository.findByCourseId(courseId);
        for (CourseModuleEntity cm : courseModules) {
            if (Boolean.TRUE.equals(cm.getDeleted())) continue;
            String moduleId = cm.getModuleId();
            if (moduleId == null || moduleId.isBlank()) continue;

            List<ModuleTopicEntity> moduleTopics = moduleTopicRepository.findByModuleId(moduleId);
            for (ModuleTopicEntity mt : moduleTopics) {
                if (Boolean.TRUE.equals(mt.getDeleted())) continue;
                String topicId = mt.getTopicId();
                if (topicId == null || topicId.isBlank()) continue;

                List<TopicLessonEntity> topicLessons = topicLessonRepository.findByTopicId(topicId);
                for (TopicLessonEntity tl : topicLessons) {
                    if (tl.getLessonId() != null && !tl.getLessonId().isBlank()) {
                        lessonIds.add(tl.getLessonId());
                    }
                }
            }
        }
        return lessonIds;
    }

    private int calculateAverageProgress(String studentId, Set<String> lessonIds) throws Exception {
        if (lessonIds == null || lessonIds.isEmpty()) {
            return 0;
        }

        int totalProgress = 0;
        for (String lessonId : lessonIds) {
            LessonProgressEntity lp = lessonProgressRepository.findByStudentAndLesson(studentId, lessonId);
            if (lp == null || Boolean.TRUE.equals(lp.getDeleted())) {
                continue; // missing = 0
            }
            int progress = lp.getProgress() == null ? 0 : Math.min(lp.getProgress(), 100);
            if (Boolean.TRUE.equals(lp.getCompleted()) && progress < 100) {
                progress = 100;
            }
            totalProgress += progress;
        }
        return Math.round((float) totalProgress / lessonIds.size());
    }

    private String resolveFullName(StudentEntity student) throws Exception {
        if (student.getProfileId() == null || student.getProfileId().isBlank()) {
            return "Chưa cập nhật";
        }
        ProfileEntity profile = profileRepository.findById(student.getProfileId());
        if (profile == null || Boolean.TRUE.equals(profile.getDeleted()) || profile.getFullName() == null) {
            return "Chưa cập nhật";
        }
        return profile.getFullName();
    }

    @Override
    public ClassAbsenceReportResponse getClassAbsenceReport(String classId, LocalDate from, LocalDate to) {
        if (classId == null || classId.isBlank()) {
            throw new BadRequestException("classId không được để trống");
        }
        if (from == null || to == null) {
            throw new BadRequestException("from, to không được để trống");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("'to' không được đứng trước 'from'");
        }

        try {
            ClassEntity classEntity = classRepository.findById(classId);
            if (classEntity == null || classEntity.isDeleted()) {
                throw new ResourceNotFoundException("Lớp học không tồn tại hoặc đã bị xóa!");
            }

            List<ClassSessionEntity> sessions = classSessionRepository.findByClassId(classId).stream()
                    .filter(s -> !Boolean.TRUE.equals(s.getDeleted()))
                    .filter(s -> s.getSessionDate() != null && !s.getSessionDate().isBlank())
                    .filter(s -> {
                        LocalDate d = LocalDate.parse(s.getSessionDate());
                        return !d.isBefore(from) && !d.isAfter(to);
                    })
                    .sorted((a, b) -> a.getSessionDate().compareTo(b.getSessionDate()))
                    .toList();

            Map<String, List<ClassAbsenceReportResponse.AbsenceDetail>> byStudent = new LinkedHashMap<>();

            for (ClassSessionEntity session : sessions) {
                List<AttendanceEntity> attendances =
                        attendanceRepository.findBySession(classId, session.getId());

                for (AttendanceEntity att : attendances) {
                    if (att.getStatus() == null || !ABSENT_STATUSES.contains(att.getStatus().trim())) {
                        continue;
                    }
                    byStudent.computeIfAbsent(att.getStudentId(), k -> new ArrayList<>()).add(ClassAbsenceReportResponse.AbsenceDetail.builder()
                                    .sessionId(session.getId())
                                    .sessionTitle(session.getTitle())
                                    .sessionDate(session.getSessionDate())
                                    .status(att.getStatus().trim())
                                    .note(att.getNote() != null ? att.getNote() : "")
                                    .build());
                }
            }

            List<ClassAbsenceReportResponse.StudentAbsence> students = new ArrayList<>();
            int totalAbsences = 0;

            for (var e : byStudent.entrySet()) {
                String studentId = e.getKey();
                List<ClassAbsenceReportResponse.AbsenceDetail> details = e.getValue();
                totalAbsences += details.size();

                String fullName = "Chưa cập nhật";
                StudentEntity student = studentRepository.findById(studentId);
                if (student != null && !Boolean.TRUE.equals(student.getDeleted())) {
                    fullName = resolveFullName(student);
                }

                students.add(ClassAbsenceReportResponse.StudentAbsence.builder()
                        .studentId(studentId)
                        .fullName(fullName)
                        .absenceCount(details.size())
                        .details(details)
                        .build());
            }

            students.sort((a, b) -> Integer.compare(b.getAbsenceCount(), a.getAbsenceCount()));

            return ClassAbsenceReportResponse.builder()
                    .classId(classId)
                    .className(classEntity.getClassName())
                    .from(from.toString())
                    .to(to.toString())
                    .totalAbsences(totalAbsences)
                    .totalStudentsAbsent(students.size())
                    .students(students)
                    .build();
        } catch (ResourceNotFoundException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi lấy báo cáo vắng theo lớp: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportDashboardResponse getReportDashboard(String yearMonth, String absenceClassId, String progressClassId) {
        YearMonth ym = (yearMonth == null || yearMonth.isBlank()) ? YearMonth.now() : YearMonth.parse(yearMonth.trim());
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        ClassAbsenceReportResponse classAbsences = null;
        if (absenceClassId != null && !absenceClassId.isBlank()) {
            classAbsences = getClassAbsenceReport(absenceClassId, from, to);
        }

        NewStudentReportResponse newStudents = getNewStudentReport("MONTH", ym.toString(), "PREVIOUS", null);

        ClassProgressReportResponse classProgress = null;
        if (progressClassId != null && !progressClassId.isBlank()) {
            classProgress = getClassProgressReport(progressClassId);
        }

        return ReportDashboardResponse.builder()
                .classAbsences(classAbsences)
                .newStudents(newStudents)
                .classProgress(classProgress)
                .build();
    }
}