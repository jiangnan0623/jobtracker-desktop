package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jobtracker.entity.Resume;
import com.jobtracker.vo.ResumeUsageVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ResumeService extends IService<Resume> {
    Resume upload(MultipartFile file, String versionName, String remark);
    Resume updateInfo(Long id, String versionName, String remark);
    List<ResumeUsageVO> usage();
    Map<Long, ResumeUsageVO> usageMap();
    Resource download(Long id);
    void removeResume(Long id);
}
