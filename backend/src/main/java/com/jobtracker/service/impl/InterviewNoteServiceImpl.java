package com.jobtracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.config.StorageProperties;
import com.jobtracker.entity.InterviewNote;
import com.jobtracker.mapper.InterviewNoteMapper;
import com.jobtracker.service.InterviewNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewNoteServiceImpl extends ServiceImpl<InterviewNoteMapper, InterviewNote> implements InterviewNoteService {
    private final StorageProperties storageProperties;

    @Override
    public InterviewNote createOrUpdateNote(InterviewNote note) {
        if (note.getJobId() == null) {
            throw new IllegalArgumentException("岗位 ID 不能为空");
        }
        if (!StringUtils.hasText(note.getTitle())) {
            throw new IllegalArgumentException("笔记标题不能为空");
        }
        String fileName = StringUtils.hasText(note.getFileName()) ? note.getFileName() : safeName(note.getTitle()) + ".md";
        if (!fileName.endsWith(".md")) {
            fileName = fileName + ".md";
        }
        Path dir = Path.of(storageProperties.getNoteDir()).toAbsolutePath().normalize();
        Path path = dir.resolve(fileName).normalize();
        if (!path.startsWith(dir)) {
            throw new IllegalArgumentException("非法文件名");
        }
        try {
            Files.createDirectories(dir);
            Files.writeString(path, note.getContent() == null ? "" : note.getContent(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("笔记保存失败：" + e.getMessage());
        }
        note.setFileName(fileName);
        note.setFilePath(path.toString());
        if (note.getId() == null) {
            save(note);
        } else {
            updateById(note);
        }
        return note;
    }

    @Override
    public List<InterviewNote> listByJobId(Long jobId) {
        return list(new LambdaQueryWrapper<InterviewNote>()
                .eq(InterviewNote::getJobId, jobId)
                .orderByDesc(InterviewNote::getUpdatedTime));
    }

    @Override
    public Resource download(Long id) {
        InterviewNote note = getById(id);
        if (note == null) {
            throw new IllegalArgumentException("笔记不存在");
        }
        return new FileSystemResource(note.getFilePath());
    }

    private String safeName(String value) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return value.replaceAll("[\\\\/:*?\"<>|]", "_") + "-" + date;
    }
}
