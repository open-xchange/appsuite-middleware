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
