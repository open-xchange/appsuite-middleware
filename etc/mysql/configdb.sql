#@(#) configdb.sql


CREATE TABLE configdb_sequence (
    id bigint AUTO_INCREMENT,
    PRIMARY KEY (id)
);

DELIMITER //
CREATE PROCEDURE get_configdb_id() NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    INSERT INTO configdb_sequence VALUES ();
    SELECT last_insert_id();
END //
DELIMITER ;

CREATE TABLE db_pool (
    db_pool_id bigint,
    url varchar(255),
    driver varchar(255),
    login varchar(255),
    password varchar(255),
    hardlimit int,
    max int,
    initial int,
    name varchar(255)
);

CREATE TABLE db_cluster (
    cluster_id bigint,
    read_db_pool_id bigint,
    write_db_pool_id bigint,
    weight int,
    max_units int
);

CREATE TABLE reason_text (
    id integer,
    text text
);

CREATE TABLE context (
    cid integer,
    name text,
    enabled boolean,
    reason_id integer,
    filestore_id bigint,
    filestore_name text,
    filestore_login text,
    filestore_passwd text,
    quota_max int8
);

CREATE TABLE filestore (
    id integer,
    uri text,
    size bigint,
    max_context integer
);

CREATE TABLE server (
    server_id bigint,
    name varchar(255)
);

CREATE TABLE server2db_pool (
    id bigint,
    server_id bigint,
    db_pool_id bigint,
    default_read boolean,
    default_write boolean
);

CREATE TABLE login2context (
    cid integer,
    login_info varchar(255)
);

CREATE TABLE context_server2db_pool (
    server_id bigint,
    cid integer,
    read_db_pool_id bigint,
    write_db_pool_id bigint,
    db_schema text
);
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

#@(#) configdb.sql consistency

INSERT INTO configdb_sequence VALUES (0);

DROP PROCEDURE IF EXISTS get_configdb_id;
DELIMITER //
CREATE PROCEDURE get_configdb_id() NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    DECLARE identifier INT4 UNSIGNED;
    SET identifier = 0;
    SELECT id INTO identifier FROM configdb_sequence FOR UPDATE;
    IF 0 = identifier THEN
        INSERT INTO configdb_sequence (id) VALUES (identifier);
    END IF;
    SET identifier = identifier + 1;
    UPDATE configdb_sequence SET id = identifier;
    SELECT identifier;
END//
DELIMITER ;

ALTER TABLE db_pool
	MODIFY `db_pool_id` INT4 UNSIGNED NOT NULL,
	MODIFY `url` varchar(255) NOT NULL,
	MODIFY `driver` varchar(128) NOT NULL,
	MODIFY `login` varchar(128) NOT NULL,
	MODIFY `password` varchar(128) NOT NULL,
	MODIFY `name` varchar(128) NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE db_cluster
	MODIFY `cluster_id` INT4 UNSIGNED NOT NULL,
	MODIFY `read_db_pool_id` INT4 UNSIGNED NOT NULL,
	MODIFY `write_db_pool_id` INT4 UNSIGNED NOT NULL,
	MODIFY `weight` INT4 UNSIGNED,
	MODIFY `max_units` INT4,
    ADD FOREIGN KEY(write_db_pool_id) REFERENCES db_pool (db_pool_id),
    ENGINE=InnoDB;

ALTER TABLE reason_text
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY text VARCHAR(255) NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE context
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY name VARCHAR(128) NOT NULL,
    MODIFY enabled boolean,
    ENGINE=InnoDB;

ALTER TABLE filestore
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY uri VARCHAR(255) NOT NULL,
    MODIFY size INT8 UNSIGNED NOT NULL,
    MODIFY max_context INT4,
    ADD CONSTRAINT filestore_uri_unique UNIQUE(uri),
    ENGINE=InnoDB;


ALTER TABLE server
    MODIFY server_id INT4 UNSIGNED NOT NULL,
    MODIFY name varchar(255) NOT NULL,
    ADD CONSTRAINT server_name_unique UNIQUE (name),
    ENGINE=InnoDB;

ALTER TABLE server2db_pool
	MODIFY `id` INT4 UNSIGNED NOT NULL,
	MODIFY `server_id` INT4 UNSIGNED NOT NULL,
	MODIFY `db_pool_id` INT4 UNSIGNED NOT NULL,
	MODIFY `default_read` boolean NOT NULL,
	MODIFY `default_write` boolean NOT NULL,
    ADD CONSTRAINT server2db_pool_unique UNIQUE(server_id, db_pool_id),
    ADD FOREIGN KEY(db_pool_id) REFERENCES db_pool (db_pool_id),
    ADD FOREIGN KEY(server_id) REFERENCES server (server_id),
    ENGINE=InnoDB;

ALTER TABLE login2context
	MODIFY `cid` INT4 UNSIGNED NOT NULL,
	MODIFY `login_info` varchar(128) NOT NULL,
    ADD FOREIGN KEY(`cid`) REFERENCES context (`cid`),
    ENGINE=InnoDB;

ALTER TABLE context_server2db_pool
	MODIFY `server_id` INT4 UNSIGNED NOT NULL,
	MODIFY `cid` INT4 UNSIGNED NOT NULL,
	MODIFY `read_db_pool_id` INT4 UNSIGNED NOT NULL,
	MODIFY `write_db_pool_id` INT4 UNSIGNED NOT NULL,
	MODIFY `db_schema` VARCHAR(32) NOT NULL,
    ADD FOREIGN KEY(`cid`) REFERENCES context (`cid`) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=InnoDB;


   
