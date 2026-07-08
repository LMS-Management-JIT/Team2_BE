package com.lms.backend.utils;

import com.lms.backend.entity.HistoryEntity;

import java.util.Objects;

public final class HistoryChangeUtils {

    private HistoryChangeUtils() {
    }
    public static HistoryEntity.ChangeDetail changed(Object oldValue, Object newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return null;
        }
        return HistoryEntity.ChangeDetail.builder()
                .oldValue(oldValue == null ? null : oldValue.toString())
                .newValue(newValue == null ? null : newValue.toString())
                .build();
    }
}