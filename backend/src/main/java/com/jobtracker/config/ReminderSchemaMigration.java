package com.jobtracker.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ReminderSchemaMigration {
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @PostConstruct
    public void migrate() {
        addColumnIfMissing("end_time", "TIMESTAMP");
        addColumnIfMissing("schedule_type", "VARCHAR(40) DEFAULT '自定义'");
        addColumnIfMissing("priority", "VARCHAR(20) DEFAULT '中'");
        addColumnIfMissing("importance", "VARCHAR(20) DEFAULT '普通'");
        addColumnIfMissing("related_application_id", "BIGINT");
        createIndexIfPossible();
    }

    private void addColumnIfMissing(String columnName, String definition) {
        if (hasColumn(columnName)) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE reminder ADD COLUMN " + columnName + " " + definition);
    }

    private boolean hasColumn(String columnName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(null, null, "REMINDER", columnName.toUpperCase(Locale.ROOT))) {
                if (columns.next()) {
                    return true;
                }
            }
            try (ResultSet columns = metaData.getColumns(null, null, "reminder", columnName)) {
                return columns.next();
            }
        } catch (Exception e) {
            throw new IllegalStateException("检查提醒表结构失败", e);
        }
    }

    private void createIndexIfPossible() {
        try {
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_reminder_related_application ON reminder (related_application_id)");
        } catch (Exception ignored) {
            // MySQL does not support IF NOT EXISTS for indexes in older versions; schema.sql/migration covers web deployments.
        }
    }
}
