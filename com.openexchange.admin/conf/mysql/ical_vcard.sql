#@(#) Tables for WebDAV iCal and vCard interfaces.

CREATE TABLE ical_principal (
    object_id INT4 UNSIGNED NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    principal text NOT NULL,
    calendarfolder INT4 UNSIGNED NOT NULL,
    taskfolder INT4 UNSIGNED NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE ical_ids (
    object_id INT4 UNSIGNED NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    principal_id INT4 UNSIGNED NOT NULL,
    client_id text,
    target_object_id int,
    module int
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE vcard_principal (
    object_id int NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    principal text,
    contactfolder int
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE vcard_ids (
    object_id int NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    principal_id int,
    client_id text,
    target_object_id int
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
