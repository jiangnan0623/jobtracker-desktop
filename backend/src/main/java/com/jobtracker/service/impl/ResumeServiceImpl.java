package com.jobtracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.config.StorageProperties;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.Resume;
import com.jobtracker.mapper.JobApplicationMapper;
import com.jobtracker.mapper.ResumeMapper;
import com.jobtracker.service.ResumeService;
import com.jobtracker.vo.ResumeUsageVO;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {
    private static final Set<String> ALLOWED_TYPES = Set.of("pdf", "doc", "docx");
    private final StorageProperties storageProperties;
    private final JobApplicationMapper jobApplicationMapper;

    @Override
    public Resume upload(MultipartFile file, String versionName, String resumeCategory, String remark) {
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
        resume.setResumeCategory(resumeCategory);
        resume.setRemark(remark);
        save(resume);
        return resume;
    }

    @Override
    public Resume updateInfo(Long id, String versionName, String resumeCategory, String remark) {
        Resume resume = getById(id);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        if (StringUtils.hasText(versionName)) {
            resume.setVersionName(versionName);
        }
        resume.setResumeCategory(resumeCategory);
        resume.setRemark(remark);
        updateById(resume);
        return resume;
    }

    @Override
    public List<ResumeUsageVO> usage() {
        return new ArrayList<>(usageMap().values());
    }

    @Override
    public Map<Long, ResumeUsageVO> usageMap() {
        Map<Long, ResumeUsageVO> result = list().stream()
                .collect(Collectors.toMap(
                        Resume::getId,
                        resume -> {
                            ResumeUsageVO vo = new ResumeUsageVO();
                            vo.setResumeId(resume.getId());
                            vo.setBindCount(0);
                            vo.setApplications(new ArrayList<>());
                            return vo;
                        },
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        if (result.isEmpty()) {
            return result;
        }

        List<JobApplication> applications = jobApplicationMapper.selectList(new LambdaQueryWrapper<JobApplication>()
                .in(JobApplication::getResumeId, result.keySet())
                .orderByDesc(JobApplication::getAppliedTime)
                .orderByDesc(JobApplication::getUpdatedTime));
        for (JobApplication application : applications) {
            if (application.getResumeId() == null) {
                continue;
            }
            ResumeUsageVO usage = result.get(application.getResumeId());
            if (usage == null) {
                continue;
            }
            ResumeUsageVO.ApplicationUsage item = new ResumeUsageVO.ApplicationUsage();
            item.setId(application.getId());
            item.setCompanyName(application.getCompanyName());
            item.setPositionName(application.getPositionName());
            item.setCurrentStatus(application.getCurrentStatus());
            usage.getApplications().add(item);
        }
        for (ResumeUsageVO item : result.values()) {
            item.setBindCount(item.getApplications().size());
        }
        return result;
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
