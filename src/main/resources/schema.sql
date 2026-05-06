-- These fixes cannot be applied by ddl-auto=update because Hibernate only adds new columns/tables,
-- it never relaxes existing NOT NULL constraints or extends existing ENUM column definitions.

-- Allow DM messages where study_group_id is intentionally null
ALTER TABLE message_table MODIFY COLUMN study_group_id BIGINT NULL;

-- Add DIRECT_MESSAGE to the notification type enum
ALTER TABLE notification_table MODIFY COLUMN type
    ENUM('SESSION_SCHEDULED','SESSION_RESCHEDULED','SESSION_CANCELLED','BADGE_EARNED','MEMBER_JOINED','DIRECT_MESSAGE')
    NOT NULL;
