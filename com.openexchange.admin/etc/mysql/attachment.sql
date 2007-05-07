CREATE TABLE prg_attachment (
	cid INTEGER,
    id INTEGER,
	created_by INTEGER,
	creation_date TIMESTAMP,
	file_mimetype TEXT,
	file_size INTEGER,
	filename TEXT,
	attached INTEGER,
	module INTEGER,
	rtf_flag BOOLEAN,
	comment TEXT,
	file_id TEXT
);

CREATE TABLE del_attachment (
	cid INTEGER,
	id INTEGER,
	attached INTEGER,
	module INTEGER,
	del_date TIMESTAMP
);ALTER TABLE prg_attachment
    MODIFY cid INT4 UNSIGNED ,
    MODIFY id INT4 UNSIGNED ,
    MODIFY creation_date INT8 ,
    MODIFY created_by INT4 UNSIGNED ,
	MODIFY attached INT4 UNSIGNED ,
	MODIFY module INT4 UNSIGNED ,
	MODIFY file_mimetype VARCHAR(255) ,
	MODIFY filename VARCHAR(255) ,
	MODIFY comment VARCHAR(255) ,
	MODIFY file_id VARCHAR(255) ,
	MODIFY file_size INT4 UNSIGNED ,
	ADD PRIMARY KEY (cid, id),
	ADD INDEX(cid, attached, module),
	ENGINE=InnoDB;


ALTER TABLE del_attachment
    MODIFY cid INT4 UNSIGNED ,
    MODIFY id INT4 UNSIGNED ,
    MODIFY del_date INT8 ,
	MODIFY attached INT4 UNSIGNED ,
	MODIFY module INT4 UNSIGNED ,
	ADD PRIMARY KEY (cid, id),
	ADD INDEX (cid, attached, module),
	ENGINE=InnoDB;
    ALTER TABLE prg_attachment
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY creation_date INT8 NOT NULL,
    MODIFY created_by INT4 UNSIGNED NOT NULL,
	MODIFY attached INT4 UNSIGNED NOT NULL,
	MODIFY module INT4 UNSIGNED NOT NULL,
	MODIFY file_mimetype VARCHAR(255) NOT NULL,
	MODIFY filename VARCHAR(255) NOT NULL,
	MODIFY comment VARCHAR(255),
	MODIFY file_id VARCHAR(255) NOT NULL,
	MODIFY file_size INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;


ALTER TABLE del_attachment
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY del_date INT8 NOT NULL,
	MODIFY attached INT4 UNSIGNED NOT NULL,
	MODIFY module INT4 UNSIGNED NOT NULL,
    ENGINE=InnoDB;
    