package com.jobtracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.mapper.InterviewRecordMapper;
import com.jobtracker.service.InterviewRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewRecordServiceImpl extends ServiceImpl<InterviewRecordMapper, InterviewRecord> implements InterviewRecordService {
    @Override
    public List<InterviewRecord> listByJobId(Long jobId) {
        return list(new LambdaQueryWrapper<InterviewRecord>()
                .eq(InterviewRecord::getJobId, jobId)
                .orderByAsc(InterviewRecord::getInterviewTime));
    }
}
