
CREATE TABLE oxfolder_tree (
        fuid int,
        cid int,
        parent int,
        fname text,
        module text,
        type text,
        creating_date bigint,
        created_from int,
        changing_date bigint,
        changed_from int,
        permission_flag int,
        subfolder_flag int,
        default_flag int
);
  	 
CREATE TABLE oxfolder_permissions (
        cid int,
	fuid int,
	permission_id int,
	fp int,	
        orp int,
        owp int,
        odp int,
        admin_flag int,
        group_flag int
);

CREATE TABLE oxfolder_specialfolders (
       tag text,
       cid int,
       fuid int
);

CREATE table oxfolder_userfolders (
		module text,
		cid int,
		linksite text,
		target text,
		img text
);

CREATE TABLE oxfolder_userfolders_standardfolders (
		owner int,
		cid int,
		module text,
		fuid int
);

CREATE TABLE del_oxfolder_tree (
		fuid int,
        cid int,
        parent int,
        fname text,
        module text,
        type text,
        creating_date bigint,
        created_from int,
        changing_date bigint,
        changed_from int,
        permission_flag int,
        subfolder_flag int,
        default_flag int
);
  	 
CREATE TABLE del_oxfolder_permissions (
        cid int,
        fuid int,
        permission_id int,
        fp int,
        orp int,
        owp int,
        odp int,
        admin_flag int,
        group_flag int
);

CREATE TABLE oxfolder_lock (
    cid INTEGER,
    id INTEGER,
    userid INTEGER,
    entity INTEGER,
    timeout BIGINT,
    depth INTEGER,
    type INT,
    scope INT,
    ownerDesc TEXT
);

CREATE TABLE oxfolder_property (
    cid INTEGER,
    id INTEGER,
    name TEXT,
    namespace TEXT,
    value TEXT,
    language TEXT,
    xml BOOLEAN
);

