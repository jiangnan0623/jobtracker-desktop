package com.jobtracker.service.impl;

import com.jobtracker.config.StorageProperties;
import com.jobtracker.dto.StorageSettingDTO;
import com.jobtracker.service.StorageSettingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class StorageSettingServiceImpl implements StorageSettingService {
    private final StorageProperties storageProperties;

    @Value("${JOB_TRACKER_DATA_DIR:./desktop-data}")
    private String dataDir;

    @PostConstruct
    public void loadSavedSettings() {
        Path file = settingFile();
        if (!Files.exists(file)) {
            createDirectories(storageProperties.getResumeDir(), storageProperties.getNoteDir(), storageProperties.getGeneralNoteDir());
            return;
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            storageProperties.setResumeDir(properties.getProperty("resumeDir", storageProperties.getResumeDir()));
            storageProperties.setNoteDir(properties.getProperty("noteDir", storageProperties.getNoteDir()));
            storageProperties.setGeneralNoteDir(properties.getProperty("generalNoteDir", storageProperties.getGeneralNoteDir()));
            createDirectories(storageProperties.getResumeDir(), storageProperties.getNoteDir(), storageProperties.getGeneralNoteDir());
        } catch (IOException e) {
            throw new IllegalStateException("读取存储设置失败：" + e.getMessage(), e);
        }
    }

    @Override
    public StorageSettingDTO get() {
        StorageSettingDTO dto = new StorageSettingDTO();
        dto.setResumeDir(storageProperties.getResumeDir());
        dto.setNoteDir(storageProperties.getNoteDir());
        dto.setGeneralNoteDir(storageProperties.getGeneralNoteDir());
        return dto;
    }

    @Override
    public StorageSettingDTO update(StorageSettingDTO dto) {
        String resumeDir = normalize(dto.getResumeDir());
        String noteDir = normalize(dto.getNoteDir());
        String generalNoteDir = normalize(dto.getGeneralNoteDir());
        createDirectories(resumeDir, noteDir, generalNoteDir);
        storageProperties.setResumeDir(resumeDir);
        storageProperties.setNoteDir(noteDir);
        storageProperties.setGeneralNoteDir(generalNoteDir);
        saveSettings();
        return get();
    }

    private String normalize(String value) {
        return Path.of(value).toAbsolutePath().normalize().toString();
    }

    private void createDirectories(String... dirs) {
        for (String dir : dirs) {
            try {
                Files.createDirectories(Path.of(dir).toAbsolutePath().normalize());
            } catch (IOException e) {
                throw new IllegalArgumentException("目录不可用：" + dir + "，" + e.getMessage());
            }
        }
    }

    private void saveSettings() {
        Properties properties = new Properties();
        properties.setProperty("resumeDir", storageProperties.getResumeDir());
        properties.setProperty("noteDir", storageProperties.getNoteDir());
        properties.setProperty("generalNoteDir", storageProperties.getGeneralNoteDir());
        Path file = settingFile();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                properties.store(output, "JobTracker desktop storage settings");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("保存存储设置失败：" + e.getMessage());
        }
    }

    private Path settingFile() {
        return Path.of(dataDir).toAbsolutePath().normalize().resolve("storage.properties");
    }
}
