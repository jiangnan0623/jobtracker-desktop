USE job_tracker;

ALTER TABLE job_application
  ADD COLUMN recruitment_type VARCHAR(40) NULL AFTER position_type,
  ADD COLUMN resume_category VARCHAR(80) NULL AFTER recruitment_type,
  ADD COLUMN job_description TEXT NULL AFTER job_link,
  ADD INDEX idx_application_recruitment_type (recruitment_type),
  ADD INDEX idx_application_resume (resume_id);
