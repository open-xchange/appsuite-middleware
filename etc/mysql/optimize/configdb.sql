#@(#) configdb.sql optimizations


ALTER TABLE configdb_sequence
    MODIFY id INT4 UNSIGNED AUTO_INCREMENT;

ALTER TABLE db_pool
	MODIFY `db_pool_id` INT4 UNSIGNED,
	MODIFY `url` varchar(255),
	MODIFY `driver` varchar(128),
	MODIFY `login` varchar(128),
	MODIFY `password` varchar(128),
	MODIFY `hardlimit` int,
	MODIFY `max` int,
	MODIFY `initial` int,
	MODIFY `name` varchar(128),
	ADD PRIMARY KEY (db_pool_id),
	ADD INDEX (name),
    DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

ALTER TABLE db_cluster
	MODIFY `cluster_id` INT4 UNSIGNED,
	MODIFY `read_db_pool_id` INT4 UNSIGNED,
	MODIFY `write_db_pool_id` INT4 UNSIGNED,
	MODIFY `weight` INT4 UNSIGNED,
	MODIFY `max_units` INT4,
	ADD PRIMARY KEY (cluster_id),
	ADD INDEX (write_db_pool_id),
    DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

ALTER TABLE reason_text
    MODIFY id INT4 UNSIGNED,
    MODIFY text VARCHAR(255),
    ADD PRIMARY KEY (id),
    DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

ALTER TABLE context
    MODIFY cid INT4 UNSIGNED,
    MODIFY name VARCHAR(128),
    MODIFY reason_id INT4 UNSIGNED,
    MODIFY enabled tinyint(1),
    MODIFY filestore_id INT4 UNSIGNED,
    MODIFY filestore_name VARCHAR(32),
    MODIFY filestore_login VARCHAR(32),
    MODIFY filestore_passwd VARCHAR(32),
    ADD PRIMARY KEY (cid),
    ADD INDEX (filestore_id),
    DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

ALTER TABLE filestore 
    MODIFY id INT4 UNSIGNED,
    MODIFY uri varchar(255),
    MODIFY size INT8 UNSIGNED,
    MODIFY max_context INT4 UNSIGNED,
    ADD PRIMARY KEY (id),
    ADD INDEX (max_context),
    DEFAULT CHARSET=latin1;

ALTER TABLE server
    MODIFY server_id INT4 UNSIGNED,
    MODIFY name varchar(255),
    ADD PRIMARY KEY (server_id),
	DEFAULT CHARSET=latin1;

ALTER TABLE server2db_pool
	MODIFY `id` INT4 UNSIGNED,
	MODIFY `server_id` INT4 UNSIGNED,
	MODIFY `db_pool_id` INT4 UNSIGNED,
	MODIFY `default_read` boolean,
	MODIFY `default_write` boolean,
	ADD PRIMARY KEY (`id`),
	DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

ALTER TABLE login2context
	MODIFY `cid` INT4 UNSIGNED,
	MODIFY `login_info` varchar(128),
	ADD PRIMARY KEY (`login_info`),
	DEFAULT CHARSET=latin1;

ALTER TABLE context_server2db_pool
	MODIFY `server_id` INT4 UNSIGNED,
	MODIFY `cid` INT4 UNSIGNED,
	MODIFY `read_db_pool_id` INT4 UNSIGNED,
	MODIFY `write_db_pool_id` INT4 UNSIGNED,
	MODIFY `db_schema` VARCHAR(32),
	ADD PRIMARY KEY(`cid`, `server_id`),
	ADD INDEX (write_db_pool_id),
	ADD INDEX (server_id),
	ADD INDEX (db_schema),
	DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

