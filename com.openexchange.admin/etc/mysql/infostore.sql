CREATE TABLE `infostore` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `folder_id` int(10) unsigned NOT NULL,
  `version` int(10) unsigned NOT NULL,
  `locked_until` bigint(64) unsigned ,
  `color_label` int(10) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `last_modified` bigint(64) NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `changed_by` int(10) unsigned ,
  PRIMARY KEY  (`cid`,`id`,`folder_id`),
  INDEX `last_modified` (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `infostore_document` (
  `cid` int(10) unsigned NOT NULL,
  `infostore_id` int(10) unsigned NOT NULL,
  `version_number` int(10) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `last_modified` bigint(64) NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `changed_by` int(10) unsigned ,
  `title` varchar(128) ,
  `url` varchar(128) ,
  `description` text,
  `categories` varchar(255) ,
  `filename` varchar(255) ,
  `file_store_location` varchar(255) ,
  `file_size` int(10) unsigned ,
  `file_mimetype` varchar(255) ,
  `file_md5sum` varchar(32)  ,
  `file_version_comment` text,
  PRIMARY KEY  (`cid`,`infostore_id`,`version_number`),
  INDEX `last_modified` (last_modified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `del_infostore` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `folder_id` int(10) unsigned NOT NULL,
  `version` int(10) unsigned NOT NULL,
  `color_label` int(10) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `last_modified` bigint(64) NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `changed_by` int(10) unsigned ,
  PRIMARY KEY  (`cid`,`id`,`folder_id`),
  INDEX `last_modified`  (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `del_infostore_document` (
  `cid` int(10) unsigned NOT NULL,
  `infostore_id` int(10) unsigned NOT NULL,
  `version_number` int(10) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `last_modified` bigint(64) NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `changed_by` int(10) unsigned ,
  `title` varchar(128) ,
  `url` varchar(128) ,
  `description` text ,
  `categories` varchar(255) ,
  `filename` varchar(255) ,
  `file_store_location` varchar(255) ,
  `file_size` int(10) unsigned ,
  `file_mimetype` varchar(255) ,
  `file_md5sum` varchar(32) ,
  `file_version_comment` text ,
  PRIMARY KEY  (`cid`,`infostore_id`,`version_number`),
  INDEX `last_modified`  (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `infostore_property` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `name` varchar(128) NOT NULL,
  `namespace` varchar(128) NOT NULL,
  `value` varchar(255) ,
  `language` varchar(128) ,
  `xml` tinyint(1) ,
  PRIMARY KEY  (`cid`,`id`,`name`,`namespace`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `infostore_lock` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `userid` int(10) unsigned NOT NULL,
  `entity` int(10) unsigned ,
  `timeout` bigint(64) unsigned NOT NULL,
  `type` tinyint(3) unsigned NOT NULL,
  `scope` tinyint(3) unsigned NOT NULL,
  `ownerDesc` varchar(128) ,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `lock_null` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `lock_null_lock` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `userid` int(10) unsigned ,
  `entity` int(10) unsigned ,
  `timeout` bigint(64) unsigned ,
  `type` tinyint(3) unsigned ,
  `scope` tinyint(3) unsigned ,
  `ownerDesc` varchar(128) ,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

