#@(#) sequences.sql 

DROP TABLE IF EXISTS sequence_id;
CREATE TABLE sequence_id (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_principal;
CREATE TABLE sequence_principal (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_resource;
CREATE TABLE sequence_resource (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_resource_group;
CREATE TABLE sequence_resource_group (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_folder;
CREATE TABLE sequence_folder (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_calendar;
CREATE TABLE sequence_calendar (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_contact;
CREATE TABLE sequence_contact (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_task;
CREATE TABLE sequence_task (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_project;
CREATE TABLE sequence_project (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_infostore;
CREATE TABLE sequence_infostore (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_forum;
CREATE TABLE sequence_forum (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_pinboard;
CREATE TABLE sequence_pinboard (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_attachment;
CREATE TABLE sequence_attachment (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_gui_setting;
CREATE TABLE sequence_gui_setting (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_reminder;
CREATE TABLE sequence_reminder (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_ical;
CREATE TABLE sequence_ical (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS sequence_webdav;
CREATE TABLE sequence_webdav (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS sequence_uid_number;
CREATE TABLE sequence_uid_number (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS sequence_gid_number;
CREATE TABLE sequence_gid_number (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS sequence_mail_service;
CREATE TABLE sequence_mail_service (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    PRIMARY KEY (cid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#@(#) sequences.sql consistency


ALTER TABLE sequence_id
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_principal
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_resource
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_resource_group
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_folder
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_calendar
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_contact
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_task
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_project
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_infostore
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_forum
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_pinboard
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_attachment
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_gui_setting
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_reminder
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_ical
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE sequence_webdav
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE sequence_uid_number
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
ENGINE=InnoDB;

ALTER TABLE sequence_gid_number
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
ENGINE=InnoDB;

ALTER TABLE sequence_mail_service
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
ENGINE=InnoDB;


DROP PROCEDURE IF EXISTS get_unique_id;
DELIMITER //
CREATE PROCEDURE get_unique_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_id SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_id WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_resource_id;
DELIMITER //
CREATE PROCEDURE get_resource_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_resource SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_resource WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_resource_group_id;
DELIMITER //
CREATE PROCEDURE get_resource_group_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_resource_group SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_resource_group WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_principal_id;
DELIMITER //
CREATE PROCEDURE get_principal_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_principal SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_principal WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_folder_id;
DELIMITER //
CREATE PROCEDURE get_folder_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_folder SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_folder WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_calendar_id;
DELIMITER //
CREATE PROCEDURE get_calendar_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_calendar SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_calendar WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_contact_id;
DELIMITER //
CREATE PROCEDURE get_contact_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_contact SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_contact WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_task_id;
DELIMITER //
CREATE PROCEDURE get_task_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_task SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_task WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_project_id;
DELIMITER //
CREATE PROCEDURE get_project_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_project SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_project WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_infostore_id;
DELIMITER //
CREATE PROCEDURE get_infostore_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_infostore SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_infostore WHERE cid=context;
END//
DELIMITER ;


DROP PROCEDURE IF EXISTS get_forum_id;
DELIMITER //
CREATE PROCEDURE get_forum_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_forum SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_forum WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_pinboard_id;
DELIMITER //
CREATE PROCEDURE get_pinboard_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_pinboard SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_pinboard WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_attachment_id;
DELIMITER //
CREATE PROCEDURE get_attachment_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_attachment SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_attachment WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_gui_setting_id;
DELIMITER //
CREATE PROCEDURE get_gui_setting_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_gui_setting SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_gui_setting WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_ical_id;
DELIMITER //
CREATE PROCEDURE get_ical_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_ical SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_ical WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_attachment_id;
DELIMITER //
CREATE PROCEDURE get_attachment_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_attachment SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_attachment WHERE cid=context;
END //
DELIMITER ;


DROP PROCEDURE IF EXISTS get_webdav_id;
DELIMITER //
CREATE PROCEDURE get_webdav_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_webdav SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_webdav WHERE cid=context;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS get_uid_number_id;
DELIMITER //
CREATE PROCEDURE get_uid_number_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_uid_number SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_uid_number WHERE cid=context;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS get_gid_number_id;
DELIMITER //
CREATE PROCEDURE get_gid_number_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_gid_number SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_gid_number WHERE cid=context;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS get_mail_service_id;
DELIMITER //
CREATE PROCEDURE get_mail_service_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE get_mail_service_id SET id=id+1 WHERE cid=context;
    SELECT id FROM get_mail_service_id WHERE cid=context;
END //
DELIMITER ;


