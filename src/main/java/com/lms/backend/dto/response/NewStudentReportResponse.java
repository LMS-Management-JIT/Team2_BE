package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewStudentReportResponse {
    private String periodType;
    private String currentLabel;
    private String compareLabel;
    private List<BucketCount> currentBuckets;
    private List<BucketCount> compareBuckets;
    private int currentTotal;
    private int compareTotal;
    private double growthPercent;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BucketCount {
        private String bucketLabel;
        private int count;
    }
}
