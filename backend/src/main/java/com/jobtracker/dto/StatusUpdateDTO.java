package com.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusUpdateDTO {
    @NotBlank(message = "状态不能为空")
    private String currentStatus;
}
