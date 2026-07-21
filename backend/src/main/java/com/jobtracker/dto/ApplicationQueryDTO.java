package com.jobtracker.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ApplicationQueryDTO {
    private String companyName;
    private String currentStatus;
    private String positionType;
    private String recruitmentType;
    private String resumeCategory;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime appliedStartTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime appliedEndTime;
    private String sortField = "appliedTime";
    private String sortOrder = "desc";
    private Long pageNo = 1L;
    private Long pageSize = 10L;
}
