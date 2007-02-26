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


   
