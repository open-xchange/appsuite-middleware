#@(#) sequences.sql optimizations


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
