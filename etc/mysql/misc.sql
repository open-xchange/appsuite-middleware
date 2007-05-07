
CREATE TABLE prg_links (
	firstid integer,
	firstmodule integer,
	firstfolder integer,
	secondid integer,
	secondmodule integer,
	secondfolder integer,
	cid integer,
	last_modified long,
	created_by integer
);

CREATE TABLE reminder (
		object_id INT4 UNSIGNED,
		cid INT4 UNSIGNED,
		last_modified INT8 UNSIGNED,
		target_id VARCHAR(255),
		module INT1,
		userid INT4 UNSIGNED,
		alarm DATETIME,
		recurrence TINYINT,
		description VARCHAR(1028),
		folder VARCHAR(1028)
);

CREATE TABLE filestore_usage (
		cid INTEGER,
		used BIGINT
);
#@(#) misc.sql optimization

DROP TABLE IF EXISTS prg_links;
CREATE TABLE prg_links (
	firstid int4 NOT NULL,
	firstmodule int4 NOT NULL,
	firstfolder int4 NOT NULL,
	secondid int4 NOT NULL,
	secondmodule int4 NOT NULL,
	secondfolder int4 NOT NULL,
	cid int4 NOT NULL,
	last_modified int8,
	created_by int4,
	INDEX (firstid),
	INDEX (secondid),
	INDEX(cid)
);

DROP TABLE IF EXISTS reminder;
CREATE TABLE reminder (
		object_id INT4 UNSIGNED,
		cid INT4 UNSIGNED,
		last_modified INT8 UNSIGNED,
		target_id VARCHAR(255),
		module INT1,
		userid INT4 UNSIGNED,
		alarm DATETIME,
		recurrence TINYINT,
		description VARCHAR(1028),
		folder VARCHAR(1028),
		PRIMARY KEY (cid, object_id),
		INDEX (cid, target_id),
		INDEX (cid, userid, alarm),
		INDEX (cid, userid, last_modified)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE filestore_usage 
	MODIFY cid INT4 UNSIGNED;
#@(#) misc.sql consistency


ALTER TABLE reminder
    MODIFY object_id INT4 UNSIGNED NOT NULL,
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY target_id VARCHAR(255) NOT NULL,
    MODIFY module INT1 UNSIGNED NOT NULL,
    MODIFY userid INT4 UNSIGNED NOT NULL,
    MODIFY alarm DATETIME NOT NULL,
    MODIFY recurrence TINYINT NOT NULL,
	ADD CONSTRAINT reminder_unique UNIQUE (cid, target_id, module, userid),
    ENGINE = InnoDB;

ALTER TABLE filestore_usage 
	MODIFY cid INT4 UNSIGNED NOT NULL,
	MODIFY used BIGINT NOT NULL,
	ADD PRIMARY KEY(CID),
	Engine=InnoDB;

ALTER TABLE prg_links ENGINE = InnoDB;
