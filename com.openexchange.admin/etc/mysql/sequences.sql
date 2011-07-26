#@(#) Sequences for unique identifiers. 

CREATE TABLE sequence_id (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_principal (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_resource (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_resource_group (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_folder (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_calendar (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_contact (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_task (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_project (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_infostore (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_forum (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_pinboard (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_attachment (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_gui_setting (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_reminder (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_ical (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_webdav (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_uid_number (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_gid_number (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_mail_service (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DELIMITER //
CREATE PROCEDURE get_unique_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_id SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_id WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_resource_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_resource SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_resource WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_resource_group_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_resource_group SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_resource_group WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_principal_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_principal SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_principal WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_folder_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_folder SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_folder WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_calendar_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_calendar SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_calendar WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_contact_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_contact SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_contact WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_task_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_task SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_task WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_project_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_project SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_project WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_infostore_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_infostore SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_infostore WHERE cid=context;
END//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_forum_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_forum SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_forum WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_pinboard_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_pinboard SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_pinboard WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_gui_setting_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_gui_setting SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_gui_setting WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_ical_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_ical SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_ical WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_attachment_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_attachment SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_attachment WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_webdav_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_webdav SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_webdav WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_uid_number_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_uid_number SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_uid_number WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_gid_number_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE sequence_gid_number SET id=id+1 WHERE cid=context;
    SELECT id FROM sequence_gid_number WHERE cid=context;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE get_mail_service_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE get_mail_service_id SET id=id+1 WHERE cid=context;
    SELECT id FROM get_mail_service_id WHERE cid=context;
END //
DELIMITER ;

