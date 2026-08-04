package com.jobtracker.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchApplicationIdsDTO {
    @NotEmpty(message = "请至少选择一条投递记录")
    private List<Long> ids;
}
