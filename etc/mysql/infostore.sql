CREATE TABLE infostore (
    cid INTEGER,
    id INTEGER,
    folder_id INTEGER,
    version INTEGER,
    locked_until INTEGER,
    color_label INTEGER,
    creating_date INTEGER,
    last_modified INTEGER,
    created_by INTEGER,
    changed_by INTEGER
);

CREATE TABLE infostore_document (
    cid INTEGER,
    infostore_id INTEGER,
    version_number INTEGER,
    creating_date INTEGER,
    last_modified INTEGER,
    created_by INTEGER,
    changed_by INTEGER,
    title TEXT,
    url TEXT,
    description TEXT,
    categories TEXT,
    filename TEXT,
    file_store_location TEXT,
    file_size INTEGER,
    file_mimetype TEXT,
    file_md5sum TEXT,
    file_version_comment TEXT
);

CREATE TABLE del_infostore (
    cid INTEGER,
    id INTEGER,
    folder_id INTEGER,
    version INTEGER,
    color_label INTEGER,
    creating_date INTEGER,
    last_modified INTEGER,
    created_by INTEGER,
    changed_by INTEGER    
);

CREATE TABLE del_infostore_document (
    cid INTEGER,
    infostore_id INTEGER,
    version_number INTEGER,
    creating_date INTEGER,
    last_modified INTEGER,
    created_by INTEGER,
    changed_by INTEGER,
    title TEXT,
    url TEXT,
    description TEXT,
    categories TEXT,
    filename TEXT,
    file_store_location TEXT,
    file_size INTEGER,
    file_mimetype TEXT,
    file_md5sum TEXT,
    file_version_comment TEXT
);

CREATE TABLE infostore_property (
    cid INTEGER,
    id INTEGER,
    name TEXT,
    namespace TEXT,
    value TEXT,
    language TEXT,
    xml BOOLEAN
);

CREATE TABLE infostore_lock (
    cid INTEGER,
    id INTEGER,
    userid INTEGER,
    entity INTEGER,
    timeout BIGINT,
    type INT,
    scope INT,
    ownerDesc TEXT
);

CREATE TABLE lock_null (
	cid INTEGER,
	id INTEGER,
	url TEXT
);

CREATE TABLE lock_null_lock (
    cid INTEGER,
    id INTEGER,
    userid INTEGER,
    entity INTEGER,
    timeout BIGINT,
    type INT,
    scope INT,
    ownerDesc TEXT
);
#@(#) infostore.sql optimizations

ALTER TABLE infostore
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY folder_id INT4 UNSIGNED,
    MODIFY version INT4 UNSIGNED,
    MODIFY locked_until BIGINT(64) UNSIGNED,
    MODIFY color_label INT4 UNSIGNED,
    MODIFY creating_date BIGINT(64),
    MODIFY last_modified BIGINT(64),
    MODIFY created_by INT4 UNSIGNED,
    MODIFY changed_by INT4 UNSIGNED,
    ADD PRIMARY KEY (cid, id, folder_id),
    ADD INDEX (last_modified),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE infostore_document
    MODIFY cid INT4 UNSIGNED,
    MODIFY infostore_id INT4 UNSIGNED,
    MODIFY version_number INT4 UNSIGNED,
    MODIFY creating_date BIGINT(64),
    MODIFY last_modified BIGINT(64),
    MODIFY created_by INT4 UNSIGNED,
    MODIFY changed_by INT4 UNSIGNED,
    MODIFY title VARCHAR(128),
    MODIFY url VARCHAR(128),
    MODIFY categories VARCHAR(255),
    MODIFY filename VARCHAR(255),
    MODIFY file_store_location VARCHAR(255),
    MODIFY file_size INT4 UNSIGNED,
    MODIFY file_mimetype VARCHAR(255),
    MODIFY file_md5sum VARCHAR(32),
    ADD PRIMARY KEY (cid, infostore_id, version_number),
    ADD INDEX (last_modified),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
    
ALTER TABLE del_infostore
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY folder_id INT4 UNSIGNED,
    MODIFY version INT4 UNSIGNED,
    MODIFY color_label INT4 UNSIGNED,
    MODIFY creating_date BIGINT(64),
    MODIFY last_modified BIGINT(64),
    MODIFY created_by INT4 UNSIGNED,
    MODIFY changed_by INT4 UNSIGNED,
    ADD PRIMARY KEY (cid, id, folder_id),
    ADD INDEX (last_modified),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
    
    
ALTER TABLE del_infostore_document
    MODIFY cid INT4 UNSIGNED,
    MODIFY infostore_id INT4 UNSIGNED,
    MODIFY version_number INT4 UNSIGNED,
    MODIFY creating_date BIGINT(64),
    MODIFY last_modified BIGINT(64),
    MODIFY created_by INT4 UNSIGNED,
    MODIFY changed_by INT4 UNSIGNED,
    MODIFY title VARCHAR(128),
    MODIFY url VARCHAR(128),
    MODIFY categories VARCHAR(255),
    MODIFY filename VARCHAR(255),
    MODIFY file_store_location VARCHAR(255),
    MODIFY file_size INT4 UNSIGNED,
    MODIFY file_mimetype VARCHAR(255),
    MODIFY file_md5sum VARCHAR(32),
    ADD PRIMARY KEY (cid, infostore_id, version_number),
    ADD INDEX (last_modified),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE infostore_property 
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY name VARCHAR(128),
    MODIFY namespace VARCHAR(128),
    MODIFY value VARCHAR(255),
    MODIFY language VARCHAR(255),
    ADD PRIMARY KEY (cid, id, name, namespace),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
    
ALTER TABLE infostore_lock
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY userid INT4 UNSIGNED,
    MODIFY entity INT4 UNSIGNED,
    MODIFY timeout BIGINT(64) UNSIGNED,
    MODIFY type TINYINT UNSIGNED,
    MODIFY scope TINYINT UNSIGNED,
    MODIFY ownerDesc VARCHAR(128),
    ADD PRIMARY KEY (cid, id);

ALTER TABLE lock_null
	MODIFY cid INT4 UNSIGNED,
	MODIFY id INT4 UNSIGNED,
	MODIFY url VARCHAR(255),
	ADD PRIMARY KEY (cid, id);

ALTER TABLE lock_null_lock
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY userid INT4 UNSIGNED,
    MODIFY entity INT4 UNSIGNED,
    MODIFY timeout BIGINT(64) UNSIGNED,
    MODIFY type TINYINT UNSIGNED,
    MODIFY scope TINYINT UNSIGNED,
    MODIFY ownerDesc VARCHAR(128),
    ADD PRIMARY KEY (cid, id);
#@(#) tasks.sql consistency
    
ALTER TABLE infostore
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY folder_id INT4 UNSIGNED NOT NULL,
    MODIFY version INT4 UNSIGNED NOT NULL,
    MODIFY locked_until BIGINT(64) UNSIGNED,
    MODIFY color_label INT4 UNSIGNED NOT NULL,
    MODIFY creating_date BIGINT(64) NOT NULL,
    MODIFY last_modified BIGINT(64) NOT NULL,
    MODIFY created_by INT4 UNSIGNED NOT NULL,
    MODIFY changed_by INT4 UNSIGNED,
    ENGINE=InnoDB;

ALTER TABLE infostore_document
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY infostore_id INT4 UNSIGNED NOT NULL,
    MODIFY version_number INT4 UNSIGNED NOT NULL,
    MODIFY creating_date BIGINT(64) NOT NULL,
    MODIFY last_modified BIGINT(64) NOT NULL,
    MODIFY created_by INT4 UNSIGNED NOT NULL,
    MODIFY changed_by INT4 UNSIGNED,
    MODIFY file_size INT4 UNSIGNED,
    MODIFY file_md5sum VARCHAR(32),
    ADD FOREIGN KEY (cid, infostore_id) REFERENCES infostore (cid, id),
    ENGINE=InnoDB;


ALTER TABLE del_infostore
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY folder_id INT4 UNSIGNED NOT NULL,
    MODIFY version INT4 UNSIGNED NOT NULL,
    MODIFY color_label INT4 UNSIGNED NOT NULL,
    MODIFY creating_date BIGINT(64) NOT NULL,
    MODIFY last_modified BIGINT(64) NOT NULL,
    MODIFY created_by INT4 UNSIGNED NOT NULL,
    MODIFY changed_by INT4 UNSIGNED,
    ENGINE=InnoDB;
    
ALTER TABLE del_infostore_document
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY infostore_id INT4 UNSIGNED NOT NULL,
    MODIFY version_number INT4 UNSIGNED NOT NULL,
    MODIFY creating_date BIGINT(64) NOT NULL,
    MODIFY last_modified BIGINT(64) NOT NULL,
    MODIFY created_by INT4 UNSIGNED NOT NULL,
    MODIFY changed_by INT4 UNSIGNED,
    MODIFY file_size INT4 UNSIGNED,
    MODIFY file_md5sum VARCHAR(32),
    ENGINE=InnoDB;
    
 ALTER TABLE infostore_property
     MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY name VARCHAR(128) NOT NULL,
    MODIFY namespace VARCHAR(128)NOT NULL,
    MODIFY value VARCHAR(255),
    MODIFY language VARCHAR(128),
    ENGINE=InnoDB;
    
ALTER TABLE infostore_lock
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY userid INT4 UNSIGNED NOT NULL,
    MODIFY timeout BIGINT(64) UNSIGNED NOT NULL,
    MODIFY type TINYINT UNSIGNED NOT NULL,
    MODIFY scope TINYINT UNSIGNED NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE lock_null
	MODIFY cid INT4 UNSIGNED NOT NULL,
	MODIFY id INT4 UNSIGNED NOT NULL,
	MODIFY url VARCHAR(255) NOT NULL,
	ENGINE=InnoDB;

ALTER TABLE lock_null_lock
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;
