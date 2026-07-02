package com.lms.backend.mapper;

import com.google.cloud.Timestamp;
import com.lms.backend.entity.ModuleEntity;
import com.lms.backend.dto.response.ModuleResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class ModuleMapper {

    // Định dạng hiển thị ngày tháng năm đẹp trên Postman
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ModuleResponse toResponse(ModuleEntity entity) {
        if (entity == null) return null;

        // LocalDateTime sử dụng trực tiếp hàm .format(FORMATTER) mượt mà
        String createDateStr = entity.getCreateDate() != null ? entity.getCreateDate().format(FORMATTER) : null;
        String updateDateStr = entity.getUpdateDate() != null ? entity.getUpdateDate().format(FORMATTER) : null;

        return new ModuleResponse(
                entity.getId(),
                entity.getModuleName(),
                entity.getModuleCode(),
                entity.getDescription(),
                createDateStr,
                updateDateStr,
                entity.getCreatedBy(),
                entity.getUpdateBy()
        );
    }

    // --- CHUYỂN ĐỔI TỪ TIMESTAMPS (FIREBASE) SANG LOCALDATETIME (JAVA) ---
    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toDate().toInstant()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .toLocalDateTime();
    }

    // --- CHUYỂN ĐỔI TỪ LOCALDATETIME (JAVA) SANG TIMESTAMPS (FIREBASE) ---
    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        Date date = Date.from(localDateTime.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
        return Timestamp.of(date);
    }
}