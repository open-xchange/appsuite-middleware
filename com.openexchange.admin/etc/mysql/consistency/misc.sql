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
