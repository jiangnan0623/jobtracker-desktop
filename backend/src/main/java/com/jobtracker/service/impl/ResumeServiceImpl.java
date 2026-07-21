package com.jobtracker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.config.StorageProperties;
import com.jobtracker.entity.Resume;
import com.jobtracker.mapper.ResumeMapper;
import com.jobtracker.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {
    private static final Set<String> ALLOWED_TYPES = Set.of("pdf", "doc", "docx");
    private final StorageProperties storageProperties;

    @Override
    public Resume upload(MultipartFile file, String versionName, String remark) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择简历文件");
        }
        String originalName = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String type = extension(originalName);
        if (!ALLOWED_TYPES.contains(type)) {
            throw new IllegalArgumentException("仅支持 pdf/doc/docx 格式");
        }
        Path dir = Path.of(storageProperties.getResumeDir()).toAbsolutePath().normalize();
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path path = dir.resolve(prefix + "-" + safeName(originalName)).normalize();
        try {
            Files.createDirectories(dir);
            file.transferTo(path);
        } catch (IOException e) {
            throw new IllegalArgumentException("简历上传失败：" + e.getMessage());
        }
        Resume resume = new Resume();
        resume.setFileName(originalName);
        resume.setFilePath(path.toString());
        resume.setFileSize(file.getSize());
        resume.setFileType(type);
        resume.setVersionName(StringUtils.hasText(versionName) ? versionName : originalName);
        resume.setRemark(remark);
        save(resume);
        return resume;
    }

    @Override
    public Resume updateInfo(Long id, String versionName, String remark) {
        Resume resume = getById(id);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        resume.setVersionName(versionName);
        resume.setRemark(remark);
        updateById(resume);
        return resume;
    }

    @Override
    public Resource download(Long id) {
        Resume resume = getById(id);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        return new FileSystemResource(resume.getFilePath());
    }

    @Override
    public void removeResume(Long id) {
        Resume resume = getById(id);
        if (resume == null) {
            return;
        }
        removeById(id);
        try {
            Files.deleteIfExists(Path.of(resume.getFilePath()));
        } catch (IOException ignored) {
            // Metadata deletion should still succeed when the local file was already moved.
        }
    }

    private String extension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index < 0 ? "" : fileName.substring(index + 1).toLowerCase();
    }

    private String safeName(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
