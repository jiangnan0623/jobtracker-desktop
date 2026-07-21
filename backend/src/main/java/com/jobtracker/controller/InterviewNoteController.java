package com.jobtracker.controller;

import com.jobtracker.common.Result;
import com.jobtracker.entity.InterviewNote;
import com.jobtracker.service.InterviewNoteService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notes")
public class InterviewNoteController {
    private final InterviewNoteService interviewNoteService;

    @GetMapping("/job/{jobId}")
    public Result<List<InterviewNote>> listByJob(@PathVariable Long jobId) {
        return Result.ok(interviewNoteService.listByJobId(jobId));
    }

    @PostMapping
    public Result<InterviewNote> create(@RequestBody InterviewNote note) {
        return Result.ok(interviewNoteService.createOrUpdateNote(note));
    }

    @PutMapping("/{id}")
    public Result<InterviewNote> update(@PathVariable Long id, @RequestBody InterviewNote note) {
        note.setId(id);
        return Result.ok(interviewNoteService.createOrUpdateNote(note));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        InterviewNote note = interviewNoteService.getById(id);
        Resource resource = interviewNoteService.download(id);
        String fileName = URLEncoder.encode(note.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        interviewNoteService.removeById(id);
        return Result.ok();
    }
}
