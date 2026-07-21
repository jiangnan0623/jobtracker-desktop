package com.jobtracker.vo;

import com.jobtracker.entity.InterviewNote;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.Resume;
import lombok.Data;

import java.util.List;

@Data
public class ApplicationDetailVO {
    private JobApplication application;
    private Resume resume;
    private List<InterviewRecord> interviewRecords;
    private List<InterviewNote> interviewNotes;
}
