ALTER TABLE reminder
  ADD COLUMN end_time DATETIME NULL,
  ADD COLUMN schedule_type VARCHAR(40) DEFAULT '自定义',
  ADD COLUMN priority VARCHAR(20) DEFAULT '中',
  ADD COLUMN importance VARCHAR(20) DEFAULT '普通',
  ADD COLUMN related_application_id BIGINT NULL;

CREATE INDEX idx_reminder_related_application ON reminder (related_application_id);
