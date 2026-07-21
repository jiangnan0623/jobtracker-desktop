package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jobtracker.entity.InterviewNote;
import org.springframework.core.io.Resource;

import java.util.List;

public interface InterviewNoteService extends IService<InterviewNote> {
    InterviewNote createOrUpdateNote(InterviewNote note);
    List<InterviewNote> listByJobId(Long jobId);
    Resource download(Long id);
}
