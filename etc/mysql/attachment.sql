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
);