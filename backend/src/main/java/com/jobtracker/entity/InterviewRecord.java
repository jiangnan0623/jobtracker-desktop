package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("interview_record")
@EqualsAndHashCode(callSuper = true)
public class InterviewRecord extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private String round;
    private LocalDateTime interviewTime;
    private String interviewer;
    private String questions;
    private String result;
    private Integer difficulty;
    private String summary;
}
