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
    MODIFY `fname` VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
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
    
