#@(#) Tables for the OX folder.

CREATE TABLE `oxfolder_tree` (
  `fuid` INT4 UNSIGNED NOT NULL,
  `cid` INT4 UNSIGNED NOT NULL,
  `parent` INT4 UNSIGNED NOT NULL,
  `fname` VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `module` TINYINT UNSIGNED NOT NULL,
  `type` TINYINT UNSIGNED NOT NULL,
  `creating_date` BIGINT(64) NOT NULL,
  `created_from` INT4 UNSIGNED NOT NULL,
  `changing_date` BIGINT(64) NOT NULL,
  `changed_from` INT4 UNSIGNED NOT NULL,
  `permission_flag` TINYINT UNSIGNED NOT NULL,
  `subfolder_flag` TINYINT UNSIGNED NOT NULL,
  `default_flag` TINYINT UNSIGNED NOT NULL default '0',
  PRIMARY KEY (`cid`, `fuid`),
  INDEX `parentIndex` (`cid`, `parent`),
  INDEX `typeIndex` (`cid`, `type`),
  INDEX `moduleIndex` (`cid`, `module`),
  INDEX `lastModifiedIndex` (`cid`, `changing_date`, `module`),
  FOREIGN KEY (`cid`, `created_from`) REFERENCES user (`cid`, `id`),
  FOREIGN KEY (`cid`, `changed_from`) REFERENCES user (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oxfolder_permissions` (
  `cid` INT4 UNSIGNED NOT NULL,
  `fuid` INT4 UNSIGNED NOT NULL,
  `permission_id` INT4 UNSIGNED NOT NULL,
  `fp` TINYINT UNSIGNED NOT NULL,
  `orp` TINYINT UNSIGNED NOT NULL,
  `owp` TINYINT UNSIGNED NOT NULL,
  `odp` TINYINT UNSIGNED NOT NULL,
  `admin_flag` TINYINT UNSIGNED NOT NULL,
  `group_flag` TINYINT UNSIGNED NOT NULL,
  `system` TINYINT UNSIGNED NOT NULL default '0',
  PRIMARY KEY  (`cid`, `fuid`, `permission_id`, `system`),
  INDEX `principal` (`cid`, `permission_id`, `fuid`),
  FOREIGN KEY (`cid`, `fuid`) REFERENCES oxfolder_tree (`cid`, `fuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oxfolder_specialfolders` (
  `tag` VARCHAR(16) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `cid` INT4 UNSIGNED NOT NULL,
  `fuid` INT4 UNSIGNED NOT NULL,
  PRIMARY KEY (`cid`,`fuid`,`tag`),
  FOREIGN KEY (`cid`, `fuid`) REFERENCES oxfolder_tree (`cid`, `fuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oxfolder_userfolders` (
  `module` TINYINT UNSIGNED NOT NULL,
  `cid` INT4 UNSIGNED NOT NULL,
  `linksite` VARCHAR(32) NOT NULL,
  `target` VARCHAR(32) NOT NULL,
  `img` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`cid`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oxfolder_userfolders_standardfolders` (
  `owner` INT4 UNSIGNED NOT NULL,
  `cid` INT4 UNSIGNED NOT NULL,
  `module` TINYINT UNSIGNED NOT NULL,
  `fuid` INT4 UNSIGNED NOT NULL,
  PRIMARY KEY (`owner`, `cid`, `module`, `fuid`),
  FOREIGN KEY (`cid`, `fuid`) REFERENCES oxfolder_tree (`cid`, `fuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `del_oxfolder_tree` (
  `fuid` INT4 UNSIGNED NOT NULL,
  `cid` INT4 UNSIGNED NOT NULL,
  `parent` INT4 UNSIGNED NOT NULL,
  `fname` VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `module` TINYINT UNSIGNED NOT NULL,
  `type` TINYINT UNSIGNED NOT NULL,
  `creating_date` BIGINT(64) NOT NULL,
  `created_from` INT4 UNSIGNED NOT NULL,
  `changing_date` BIGINT(64) NOT NULL,
  `changed_from` INT4 UNSIGNED NOT NULL,
  `permission_flag` TINYINT UNSIGNED NOT NULL,
  `subfolder_flag` TINYINT UNSIGNED NOT NULL,
  `default_flag` TINYINT UNSIGNED NOT NULL default '0',
  PRIMARY KEY (`cid`, `fuid`),
  INDEX `parentIndex` (`cid`, `parent`),
  INDEX `typeIndex` (`cid`, `type`),
  INDEX `moduleIndex` (`cid`, `module`),
  INDEX `lastModifiedIndex` (`cid`, `changing_date`, `module`),
  FOREIGN KEY (`cid`, `created_from`) REFERENCES user (`cid`, `id`),
  FOREIGN KEY (`cid`, `changed_from`) REFERENCES user (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `del_oxfolder_permissions` (
  `cid` INT4 UNSIGNED NOT NULL,
  `fuid` INT4 UNSIGNED NOT NULL,
  `permission_id` INT4 UNSIGNED NOT NULL,
  `fp` TINYINT UNSIGNED NOT NULL,
  `orp` TINYINT UNSIGNED NOT NULL,
  `owp` TINYINT UNSIGNED NOT NULL,
  `odp` TINYINT UNSIGNED NOT NULL,
  `admin_flag` TINYINT UNSIGNED NOT NULL,
  `group_flag` TINYINT UNSIGNED NOT NULL,
  `system` TINYINT UNSIGNED NOT NULL default '0',
  PRIMARY KEY  (`cid`,`fuid`,`permission_id`,`system`),
  INDEX `principal` (`cid`, `permission_id`, `fuid`),
  FOREIGN KEY (`cid`, `fuid`) REFERENCES del_oxfolder_tree (`cid`, `fuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oxfolder_lock` (
  `cid` INT4 UNSIGNED NOT NULL,
  `id` INT4 UNSIGNED NOT NULL,
  `userid` INT4 UNSIGNED NOT NULL,
  `entity` INT4 UNSIGNED default NULL,
  `timeout` BIGINT(64) UNSIGNED NOT NULL,
  `depth` TINYINT default NULL,
  `type` TINYINT UNSIGNED NOT NULL,
  `scope` TINYINT UNSIGNED NOT NULL,
  `ownerDesc` VARCHAR(128) default NULL,
  PRIMARY KEY (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oxfolder_property` (
  `cid` INT4 UNSIGNED NOT NULL,
  `id` INT4 UNSIGNED NOT NULL,
  `name` VARCHAR(128) COLLATE utf8_unicode_ci NOT NULL,
  `namespace` VARCHAR(128) COLLATE utf8_unicode_ci NOT NULL,
  `value` VARCHAR(255) COLLATE utf8_unicode_ci default NULL,
  `language` VARCHAR(128) COLLATE utf8_unicode_ci default NULL,
  `xml` BOOLEAN default NULL,
  PRIMARY KEY (cid, id, name, namespace)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
