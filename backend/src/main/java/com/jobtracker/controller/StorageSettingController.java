package com.jobtracker.controller;

import com.jobtracker.common.Result;
import com.jobtracker.dto.StorageSettingDTO;
import com.jobtracker.service.StorageSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")
public class StorageSettingController {
    private final StorageSettingService storageSettingService;

    @GetMapping
    public Result<StorageSettingDTO> get() {
        return Result.ok(storageSettingService.get());
    }

    @PutMapping
    public Result<StorageSettingDTO> update(@Valid @RequestBody StorageSettingDTO dto) {
        return Result.ok(storageSettingService.update(dto));
    }
}
