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

