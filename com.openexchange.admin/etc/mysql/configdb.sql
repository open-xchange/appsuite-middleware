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
