package com.jobtracker.controller;

import com.jobtracker.common.Result;
import com.jobtracker.entity.Resume;
import com.jobtracker.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeService resumeService;

    @GetMapping
    public Result<List<Resume>> list() {
        return Result.ok(resumeService.list());
    }

    @PostMapping("/upload")
    public Result<Resume> upload(@RequestParam MultipartFile file,
                                 @RequestParam(required = false) String versionName,
                                 @RequestParam(required = false) String remark) {
        return Result.ok(resumeService.upload(file, versionName, remark));
    }

    @PutMapping("/{id}")
    public Result<Resume> update(@PathVariable Long id,
                                 @RequestParam String versionName,
                                 @RequestParam(required = false) String remark) {
        return Result.ok(resumeService.updateInfo(id, versionName, remark));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resume resume = resumeService.getById(id);
        Resource resource = resumeService.download(id);
        String fileName = URLEncoder.encode(resume.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .body(resource);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        Resume resume = resumeService.getById(id);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        if (!"pdf".equalsIgnoreCase(resume.getFileType())) {
            throw new IllegalArgumentException("当前仅支持 PDF 简历预览");
        }
        Resource resource = resumeService.download(id);
        String fileName = URLEncoder.encode(resume.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + fileName)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resumeService.removeResume(id);
        return Result.ok();
    }
}
