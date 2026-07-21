package com.jobtracker.vo;

import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.Reminder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private Long totalApplications;
    private Long interviewCount;
    private Long offerCount;
    private Map<String, Long> statusCount;
    private Map<String, Long> weeklyTrend;
    private Map<String, Long> companyCount;
    private List<JobApplication> recentApplications;
    private List<Reminder> todayReminders;
}
