
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
