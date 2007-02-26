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
