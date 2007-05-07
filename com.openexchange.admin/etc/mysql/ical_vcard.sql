
CREATE TABLE ical_principal (
		object_id int,
		cid int,
		principal text,
		calendarfolder int,
		taskfolder int
);

CREATE TABLE ical_ids (
		object_id int,
		cid int,
		principal_id int,
		client_id text,
		target_object_id int,
		module int
);

CREATE TABLE vcard_principal (
		object_id int,
		cid int,
		principal text,
		contactfolder int
);

CREATE TABLE vcard_ids (
		object_id int,
		cid int,
		principal_id int,
		client_id text,
		target_object_id int
);
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