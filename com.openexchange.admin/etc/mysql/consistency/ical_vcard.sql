#@(#) ical_vcard.sql consistency


ALTER TABLE ical_principal
    MODIFY object_id int NOT NULL,
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY principal text NOT NULL,
    MODIFY calendarfolder int NOT NULL,
    MODIFY taskfolder int NOT NULL,
    ENGINE = InnoDB;

ALTER TABLE ical_ids
    MODIFY object_id int NOT NULL,
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY principal_id int NOT NULL,
    ENGINE = InnoDB;

ALTER TABLE vcard_principal
    MODIFY object_id int NOT NULL,
    MODIFY cid INT4 UNSIGNED NOT NULL,
    ENGINE = InnoDB;

ALTER TABLE vcard_ids
    MODIFY object_id int NOT NULL,
    MODIFY cid INT4 UNSIGNED NOT NULL,
    ENGINE = InnoDB;