package com.lms.backend.mapper;

import com.lms.backend.dto.response.ModuleResponseDto;
import com.lms.backend.entity.Module;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Component
public class ModuleMapper {

    public ModuleResponseDto toResponseDto(Module entity) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        String createStr = entity.getCreateDate() != null ? sdf.format(entity.getCreateDate().toDate()) : null;
        String updateStr = entity.getUpdateDate() != null ? sdf.format(entity.getUpdateDate().toDate()) : null;

        return new ModuleResponseDto(
                entity.getId(),
                entity.getModuleName(),
                entity.getModuleCode(),
                entity.getDescription(),
                createStr,
                updateStr,
                entity.getCreatedBy(),
                entity.getUpdateBy()
        );
    }
}