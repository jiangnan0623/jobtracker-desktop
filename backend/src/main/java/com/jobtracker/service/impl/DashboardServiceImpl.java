package com.jobtracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.DashboardService;
import com.jobtracker.service.InterviewRecordService;
import com.jobtracker.service.JobApplicationService;
import com.jobtracker.service.ReminderService;
import com.jobtracker.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final JobApplicationService jobApplicationService;
    private final InterviewRecordService interviewRecordService;
    private final ReminderService reminderService;

    @Override
    public DashboardVO overview() {
        List<JobApplication> applications = jobApplicationService.list();
        DashboardVO vo = new DashboardVO();
        vo.setTotalApplications((long) applications.size());
        vo.setInterviewCount(interviewRecordService.count(new LambdaQueryWrapper<InterviewRecord>()));
        vo.setOfferCount(applications.stream().filter(item -> "Offer".equals(item.getCurrentStatus())).count());
        vo.setStatusCount(applications.stream().collect(Collectors.groupingBy(JobApplication::getCurrentStatus, LinkedHashMap::new, Collectors.counting())));
        vo.setCompanyCount(applications.stream().collect(Collectors.groupingBy(JobApplication::getCompanyName, LinkedHashMap::new, Collectors.counting())));
        vo.setWeeklyTrend(weeklyTrend(applications));
        vo.setRecentApplications(applications.stream()
                .sorted((a, b) -> nullSafeTime(b).compareTo(nullSafeTime(a)))
                .limit(8)
                .toList());
        vo.setTodayReminders(reminderService.today());
        return vo;
    }

    private Map<String, Long> weeklyTrend(List<JobApplication> applications) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        Map<String, Long> trend = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            long count = applications.stream()
                    .filter(item -> item.getAppliedTime() != null)
                    .filter(item -> item.getAppliedTime().toLocalDate().equals(day))
                    .count();
            trend.put(day.format(formatter), count);
        }
        return trend;
    }

    private java.time.LocalDateTime nullSafeTime(JobApplication application) {
        if (application.getAppliedTime() != null) {
            return application.getAppliedTime();
        }
        if (application.getUpdatedTime() != null) {
            return application.getUpdatedTime();
        }
        return java.time.LocalDateTime.MIN;
    }
}
