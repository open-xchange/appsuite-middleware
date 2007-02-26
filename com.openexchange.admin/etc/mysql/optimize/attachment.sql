ALTER TABLE prg_attachment
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
    