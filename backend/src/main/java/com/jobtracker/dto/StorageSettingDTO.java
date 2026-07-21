package com.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StorageSettingDTO {
    @NotBlank(message = "简历目录不能为空")
    private String resumeDir;

    @NotBlank(message = "面试笔记目录不能为空")
    private String noteDir;

    @NotBlank(message = "普通笔记目录不能为空")
    private String generalNoteDir;
}
