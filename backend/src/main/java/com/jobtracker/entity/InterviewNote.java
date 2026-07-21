package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("interview_note")
@EqualsAndHashCode(callSuper = true)
public class InterviewNote extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private String title;
    private String fileName;
    private String filePath;
    private String content;
}
