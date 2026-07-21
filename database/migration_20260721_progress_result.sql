USE job_tracker;

ALTER TABLE job_application
  ADD COLUMN progress_result VARCHAR(40) NULL AFTER progress_flow,
  ADD COLUMN progress_operated_time DATETIME NULL AFTER progress_result;
