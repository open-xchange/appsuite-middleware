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
