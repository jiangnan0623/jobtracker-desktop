package com.jobtracker.service;

import com.jobtracker.dto.StorageSettingDTO;

public interface StorageSettingService {
    StorageSettingDTO get();
    StorageSettingDTO update(StorageSettingDTO dto);
}
