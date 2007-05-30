
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

#@(#) oxfolder.sql optimizations


ALTER TABLE oxfolder_tree
    MODIFY `fuid` INT4 UNSIGNED,
    MODIFY `cid` INT4 UNSIGNED,
    MODIFY `parent` INT4 UNSIGNED,
    MODIFY `fname` VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci,
    MODIFY `module` TINYINT UNSIGNED,
    MODIFY `type` TINYINT UNSIGNED,
    MODIFY `creating_date` BIGINT(64),
    MODIFY `created_from` INT4 UNSIGNED,
    MODIFY `changing_date` BIGINT(64),
    MODIFY `changed_from` INT4 UNSIGNED,
    MODIFY `permission_flag` TINYINT UNSIGNED,
    MODIFY `subfolder_flag` TINYINT UNSIGNED,
    MODIFY `default_flag` TINYINT UNSIGNED,
    ADD PRIMARY KEY (`cid`, `fuid`),
    ADD INDEX (`cid`, `parent`);

ALTER TABLE oxfolder_permissions
    MODIFY `cid` INT4 UNSIGNED,
    MODIFY `fuid` INT4 UNSIGNED,
    MODIFY `permission_id` INT4 UNSIGNED,
    MODIFY `fp` TINYINT UNSIGNED,
    MODIFY `orp` TINYINT UNSIGNED,
    MODIFY `owp` TINYINT UNSIGNED,
    MODIFY `odp` TINYINT UNSIGNED,
    MODIFY `admin_flag` TINYINT UNSIGNED,
    MODIFY `group_flag` TINYINT UNSIGNED,
    ADD PRIMARY KEY (`cid`,`permission_id`,`fuid`);

ALTER TABLE oxfolder_specialfolders
    MODIFY `tag` VARCHAR(16) CHARACTER SET utf8 COLLATE utf8_unicode_ci,
    MODIFY `cid` INT4 UNSIGNED,
    MODIFY `fuid` INT4 UNSIGNED,
    ADD PRIMARY KEY (`cid`, `fuid`, `tag`);

ALTER table oxfolder_userfolders
    MODIFY `module` TINYINT UNSIGNED,
    MODIFY `cid` INT4 UNSIGNED,
    MODIFY `linksite` VARCHAR(32),
    MODIFY `target` VARCHAR(32),
    MODIFY `img` VARCHAR(32),
    ADD PRIMARY KEY (`cid`, `module`);

ALTER TABLE oxfolder_userfolders_standardfolders
    MODIFY `owner` INT4 UNSIGNED,
    MODIFY `cid` INT4 UNSIGNED,
    MODIFY `module` TINYINT UNSIGNED,
    MODIFY `fuid` INT4 UNSIGNED,
    ADD PRIMARY KEY (`owner`, `cid`, `module`, `fuid`);

ALTER TABLE del_oxfolder_tree
    MODIFY `fuid` INT4 UNSIGNED,
    MODIFY `cid` INT4 UNSIGNED,
    MODIFY `parent` INT4 UNSIGNED,
    MODIFY `fname` VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci,
    MODIFY `module` TINYINT UNSIGNED,
    MODIFY `type` TINYINT UNSIGNED,
    MODIFY `creating_date` BIGINT(64),
    MODIFY `created_from` INT4 UNSIGNED,
    MODIFY `changing_date` BIGINT(64),
    MODIFY `changed_from` INT4 UNSIGNED,
    MODIFY `permission_flag` TINYINT UNSIGNED,
    MODIFY `subfolder_flag` TINYINT UNSIGNED,
    MODIFY `default_flag` TINYINT UNSIGNED,
    ADD PRIMARY KEY (`cid`, `fuid`),
    ADD INDEX (`cid`, `parent`);
  	 
ALTER TABLE del_oxfolder_permissions
    MODIFY `cid` INT4 UNSIGNED,
    MODIFY `fuid` INT4 UNSIGNED,
    MODIFY `permission_id` INT4 UNSIGNED,
    MODIFY `fp` TINYINT UNSIGNED,
    MODIFY `orp` TINYINT UNSIGNED,
    MODIFY `owp` TINYINT UNSIGNED,
    MODIFY `odp` TINYINT UNSIGNED,
    MODIFY `admin_flag` TINYINT UNSIGNED,
    MODIFY `group_flag` TINYINT UNSIGNED,
    ADD PRIMARY KEY (`cid`,`permission_id`,`fuid`);

ALTER TABLE oxfolder_lock
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY userid INT4 UNSIGNED,
    MODIFY entity INT4 UNSIGNED,
    MODIFY depth TINYINT,
    MODIFY timeout BIGINT(64) UNSIGNED,
    MODIFY type TINYINT UNSIGNED,
    MODIFY scope TINYINT UNSIGNED,
    MODIFY ownerDesc VARCHAR(128),
    ADD PRIMARY KEY (cid, id);

ALTER TABLE oxfolder_property 
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY name VARCHAR(128),
    MODIFY namespace VARCHAR(128),
    MODIFY value VARCHAR(255),
    MODIFY language VARCHAR(255),
    ADD PRIMARY KEY (cid, id, name, namespace),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
#@(#) oxfolder.sql consistency


ALTER TABLE oxfolder_tree
    MODIFY `fuid` INT4 UNSIGNED NOT NULL,
    MODIFY `cid` INT4 UNSIGNED NOT NULL,
    MODIFY `parent` INT4 UNSIGNED NOT NULL,
    MODIFY `fname` VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    MODIFY `module` TINYINT UNSIGNED NOT NULL,
    MODIFY `type` TINYINT UNSIGNED NOT NULL,
    MODIFY `creating_date` BIGINT(64) NOT NULL,
    MODIFY `created_from` INT4 UNSIGNED NOT NULL,
    MODIFY `changing_date` BIGINT(64) NOT NULL,
    MODIFY `changed_from` INT4 UNSIGNED NOT NULL,
    MODIFY `permission_flag` TINYINT UNSIGNED NOT NULL,
    MODIFY `subfolder_flag` TINYINT UNSIGNED NOT NULL,
    MODIFY `default_flag` TINYINT UNSIGNED DEFAULT 0,
    ADD FOREIGN KEY (`cid`, `created_from`) REFERENCES user (`cid`, `id`),
    ADD FOREIGN KEY (`cid`, `changed_from`) REFERENCES user (`cid`, `id`),
    ENGINE = InnoDB;

ALTER TABLE oxfolder_permissions
    MODIFY `cid` INT4 UNSIGNED NOT NULL,
    MODIFY `fuid` INT4 UNSIGNED NOT NULL,
    MODIFY `permission_id` INT4 UNSIGNED NOT NULL,
    MODIFY `fp` TINYINT UNSIGNED NOT NULL,
    MODIFY `orp` TINYINT UNSIGNED NOT NULL,
    MODIFY `owp` TINYINT UNSIGNED NOT NULL,
    MODIFY `odp` TINYINT UNSIGNED NOT NULL,
    MODIFY `admin_flag` TINYINT UNSIGNED NOT NULL,
    MODIFY `group_flag` TINYINT UNSIGNED NOT NULL,
    ADD FOREIGN KEY (`cid`, `fuid`) REFERENCES oxfolder_tree (`cid`, `fuid`),
    ENGINE = InnoDB;

ALTER TABLE oxfolder_specialfolders
    MODIFY `tag` VARCHAR(16) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    MODIFY `cid` INT4 UNSIGNED NOT NULL,
    MODIFY `fuid` INT4 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (`cid`, `fuid`) REFERENCES oxfolder_tree (`cid`, `fuid`),
    ENGINE = InnoDB;

ALTER table oxfolder_userfolders
    MODIFY `module` TINYINT UNSIGNED NOT NULL,
    MODIFY `cid` INT4 UNSIGNED NOT NULL,
    MODIFY `linksite` VARCHAR(32) NOT NULL,
    MODIFY `target` VARCHAR(32) NOT NULL,
    MODIFY `img` VARCHAR(32) NOT NULL,
    ENGINE = InnoDB;

ALTER TABLE oxfolder_userfolders_standardfolders
    MODIFY `owner` INT4 UNSIGNED NOT NULL,
    MODIFY `cid` INT4 UNSIGNED NOT NULL,
    MODIFY `module` TINYINT UNSIGNED NOT NULL,
    MODIFY `fuid` INT4 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (`cid`, `fuid`) REFERENCES oxfolder_tree (`cid`, `fuid`),
    ENGINE = InnoDB;

ALTER TABLE del_oxfolder_tree
    MODIFY `fuid` INT4 UNSIGNED NOT NULL,
    MODIFY `cid` INT4 UNSIGNED NOT NULL,
    MODIFY `parent` INT4 UNSIGNED NOT NULL,
    MODIFY `fname` VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    MODIFY `module` TINYINT UNSIGNED NOT NULL,
    MODIFY `type` TINYINT UNSIGNED NOT NULL,
    MODIFY `creating_date` BIGINT(64) NOT NULL,
    MODIFY `created_from` INT4 UNSIGNED NOT NULL,
    MODIFY `changing_date` BIGINT(64) NOT NULL,
    MODIFY `changed_from` INT4 UNSIGNED NOT NULL,
    MODIFY `permission_flag` TINYINT UNSIGNED NOT NULL,
    MODIFY `subfolder_flag` TINYINT UNSIGNED NOT NULL,
    MODIFY `default_flag` TINYINT UNSIGNED DEFAULT 0,
    ADD FOREIGN KEY (`cid`, `created_from`) REFERENCES user (`cid`, `id`),
    ADD FOREIGN KEY (`cid`, `changed_from`) REFERENCES user (`cid`, `id`),
    ENGINE = InnoDB;

ALTER TABLE del_oxfolder_permissions
    MODIFY `cid` INT4 UNSIGNED NOT NULL,
    MODIFY `fuid` INT4 UNSIGNED NOT NULL,
    MODIFY `permission_id` INT4 UNSIGNED NOT NULL,
    MODIFY `fp` TINYINT UNSIGNED NOT NULL,
    MODIFY `orp` TINYINT UNSIGNED NOT NULL,
    MODIFY `owp` TINYINT UNSIGNED NOT NULL,
    MODIFY `odp` TINYINT UNSIGNED NOT NULL,
    MODIFY `admin_flag` TINYINT UNSIGNED NOT NULL,
    MODIFY `group_flag` TINYINT UNSIGNED NOT NULL,
    ADD FOREIGN KEY (`cid`, `fuid`) REFERENCES del_oxfolder_tree (`cid`, `fuid`),
    ENGINE = InnoDB;
    
 ALTER TABLE oxfolder_lock
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY userid INT4 UNSIGNED NOT NULL,
    MODIFY timeout BIGINT(64) UNSIGNED NOT NULL,
    MODIFY type TINYINT UNSIGNED NOT NULL,
    MODIFY scope TINYINT UNSIGNED NOT NULL,
    ENGINE=InnoDB;
    
ALTER TABLE oxfolder_property
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY name VARCHAR(128) NOT NULL,
    MODIFY namespace VARCHAR(128)NOT NULL,
    MODIFY value VARCHAR(255),
    MODIFY language VARCHAR(128),
    ENGINE=InnoDB;
    
