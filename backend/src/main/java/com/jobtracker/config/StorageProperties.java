package com.jobtracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "job-tracker.storage")
public class StorageProperties {
    private String resumeDir;
    private String noteDir;
    private String generalNoteDir;
}
