package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jobtracker.entity.Resume;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService extends IService<Resume> {
    Resume upload(MultipartFile file, String versionName, String remark);
    Resume updateInfo(Long id, String versionName, String remark);
    Resource download(Long id);
    void removeResume(Long id);
}
