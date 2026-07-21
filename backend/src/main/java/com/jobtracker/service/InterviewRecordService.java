package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jobtracker.entity.InterviewRecord;

import java.util.List;

public interface InterviewRecordService extends IService<InterviewRecord> {
    List<InterviewRecord> listByJobId(Long jobId);
}
