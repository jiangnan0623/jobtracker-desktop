package com.jobtracker.controller;

import com.jobtracker.common.Result;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.service.InterviewRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interviews")
public class InterviewRecordController {
    private final InterviewRecordService interviewRecordService;

    @GetMapping("/job/{jobId}")
    public Result<List<InterviewRecord>> listByJob(@PathVariable Long jobId) {
        return Result.ok(interviewRecordService.listByJobId(jobId));
    }

    @PostMapping
    public Result<InterviewRecord> create(@RequestBody InterviewRecord record) {
        interviewRecordService.save(record);
        return Result.ok(record);
    }

    @PutMapping("/{id}")
    public Result<InterviewRecord> update(@PathVariable Long id, @RequestBody InterviewRecord record) {
        record.setId(id);
        interviewRecordService.updateById(record);
        return Result.ok(record);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        interviewRecordService.removeById(id);
        return Result.ok();
    }
}
