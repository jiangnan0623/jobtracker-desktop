package com.jobtracker.vo;

import lombok.Data;

import java.util.List;

@Data
public class ResumeUsageVO {
    private Long resumeId;
    private Integer bindCount;
    private List<ApplicationUsage> applications;

    @Data
    public static class ApplicationUsage {
        private Long id;
        private String companyName;
        private String positionName;
        private String currentStatus;
    }
}
