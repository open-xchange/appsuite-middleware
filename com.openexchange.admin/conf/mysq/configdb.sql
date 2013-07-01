#@(#) Tables for the Configuration Database

CREATE TABLE configdb_sequence (
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

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

CREATE TABLE db_pool (
    db_pool_id INT4 UNSIGNED NOT NULL,
    url VARCHAR(255) NOT NULL,
    driver VARCHAR(128) NOT NULL,
    login VARCHAR(128) NOT NULL,
    password VARCHAR(128) NOT NULL,
    hardlimit INT4,
    max INT4,
    initial INT4,
    name VARCHAR(128) NOT NULL,
    PRIMARY KEY (db_pool_id),
    INDEX (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE db_cluster (
    cluster_id INT4 UNSIGNED NOT NULL,
    read_db_pool_id INT4 UNSIGNED NOT NULL,
    write_db_pool_id INT4 UNSIGNED NOT NULL,
    weight INT4 UNSIGNED,
    max_units INT4,
    PRIMARY KEY (cluster_id),
    FOREIGN KEY(write_db_pool_id) REFERENCES db_pool (db_pool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE reason_text (
    id INT4 UNSIGNED NOT NULL,
    text VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE context (
    cid INT4 UNSIGNED NOT NULL,
    name VARCHAR(128) NOT NULL,
    enabled BOOLEAN,
    reason_id INT4 UNSIGNED,
    filestore_id INT4 UNSIGNED,
    filestore_name VARCHAR(32),
    filestore_login VARCHAR(32),
    filestore_passwd VARCHAR(32),
    quota_max INT8,
    PRIMARY KEY (cid),
    INDEX (filestore_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE filestore (
    id INT4 UNSIGNED NOT NULL,
    uri VARCHAR(255) NOT NULL,
    size INT8 UNSIGNED NOT NULL,
    max_context INT4,
    PRIMARY KEY (id),
    INDEX (max_context),
    CONSTRAINT filestore_uri_unique UNIQUE(uri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE server (
    server_id INT4 UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (server_id),
    CONSTRAINT server_name_unique UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE login2context (
    cid INT4 UNSIGNED NOT NULL,
    login_info VARCHAR(128) NOT NULL,
    PRIMARY KEY (`login_info`),
    FOREIGN KEY(`cid`) REFERENCES context (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE context_server2db_pool (
    server_id INT4 UNSIGNED NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    read_db_pool_id INT4 UNSIGNED NOT NULL,
    write_db_pool_id INT4 UNSIGNED NOT NULL,
    db_schema VARCHAR(32) NOT NULL,
    PRIMARY KEY(`cid`, `server_id`),
    INDEX (write_db_pool_id),
    INDEX (server_id),
    INDEX (db_schema),
    FOREIGN KEY(`cid`) REFERENCES context (`cid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
