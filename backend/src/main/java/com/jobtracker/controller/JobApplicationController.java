package com.jobtracker.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jobtracker.common.Result;
import com.jobtracker.dto.ApplicationQueryDTO;
import com.jobtracker.dto.StatusUpdateDTO;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.JobApplicationService;
import com.jobtracker.vo.ApplicationDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications")
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    @GetMapping
    public Result<Page<JobApplication>> page(ApplicationQueryDTO query) {
        return Result.ok(jobApplicationService.pageApplications(query));
    }

    @GetMapping("/status-options")
    public Result<List<String>> statusOptions() {
        return Result.ok(jobApplicationService.statusOptions());
    }

    @GetMapping("/position-type-options")
    public Result<List<String>> positionTypeOptions() {
        return Result.ok(jobApplicationService.positionTypeOptions());
    }

    @GetMapping("/resume-category-options")
    public Result<List<String>> resumeCategoryOptions() {
        return Result.ok(jobApplicationService.resumeCategoryOptions());
    }

    @GetMapping("/{id}")
    public Result<ApplicationDetailVO> detail(@PathVariable Long id) {
        return Result.ok(jobApplicationService.detail(id));
    }

    @PostMapping
    public Result<JobApplication> create(@RequestBody JobApplication application) {
        jobApplicationService.save(application);
        return Result.ok(application);
    }

    @PutMapping("/{id}")
    public Result<JobApplication> update(@PathVariable Long id, @RequestBody JobApplication application) {
        application.setId(id);
        jobApplicationService.updateById(application);
        return Result.ok(application);
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO dto) {
        jobApplicationService.updateStatus(id, dto.getCurrentStatus());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        jobApplicationService.removeById(id);
        return Result.ok();
    }
}
