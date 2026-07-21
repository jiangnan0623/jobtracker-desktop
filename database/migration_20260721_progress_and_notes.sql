USE job_tracker;

ALTER TABLE job_application
  ADD COLUMN progress_flow TEXT NULL AFTER current_status;

CREATE TABLE IF NOT EXISTS note_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT,
  type VARCHAR(20) NOT NULL,
  title VARCHAR(160) NOT NULL,
  file_name VARCHAR(255),
  file_path VARCHAR(500),
  content MEDIUMTEXT,
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_note_item_parent (parent_id),
  INDEX idx_note_item_type (type),
  INDEX idx_note_item_updated (updated_time),
  INDEX idx_note_item_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
