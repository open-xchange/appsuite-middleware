CREATE TABLE `infostore` (
  `cid` int4 unsigned NOT NULL,
  `id` int4 unsigned NOT NULL,
  `folder_id` int4 unsigned NOT NULL,
  `version` int4 unsigned NOT NULL,
  `locked_until` int8 unsigned ,
  `color_label` int4 unsigned NOT NULL,
  `creating_date` int8 NOT NULL,
  `last_modified` int8 NOT NULL,
  `created_by` int4 unsigned NOT NULL,
  `changed_by` int4 unsigned ,
  PRIMARY KEY  (`cid`,`id`,`folder_id`),
  INDEX `last_modified` (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `infostore_document` (
  `cid` int4 unsigned NOT NULL,
  `infostore_id` int4 unsigned NOT NULL,
  `version_number` int4 unsigned NOT NULL,
  `creating_date` int8 NOT NULL,
  `last_modified` int8 NOT NULL,
  `created_by` int4 unsigned NOT NULL,
  `changed_by` int4 unsigned ,
  `title` varchar(128) ,
  `url` varchar(256) ,
  `description` text,
  `categories` varchar(255) ,
  `filename` varchar(255) ,
  `file_store_location` varchar(255) ,
  `file_size` int4 unsigned ,
  `file_mimetype` varchar(255) ,
  `file_md5sum` varchar(32)  ,
  `file_version_comment` text,
  PRIMARY KEY  (`cid`,`infostore_id`,`version_number`),
  INDEX `last_modified` (last_modified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `del_infostore` (
  `cid` int4 unsigned NOT NULL,
  `id` int4 unsigned NOT NULL,
  `folder_id` int4 unsigned NOT NULL,
  `version` int4 unsigned NOT NULL,
  `color_label` int4 unsigned NOT NULL,
  `creating_date` int8 NOT NULL,
  `last_modified` int8 NOT NULL,
  `created_by` int4 unsigned NOT NULL,
  `changed_by` int4 unsigned ,
  PRIMARY KEY  (`cid`,`id`,`folder_id`),
  INDEX `last_modified`  (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `del_infostore_document` (
  `cid` int4 unsigned NOT NULL,
  `infostore_id` int4 unsigned NOT NULL,
  `version_number` int4 unsigned NOT NULL,
  `creating_date` int8 NOT NULL,
  `last_modified` int8 NOT NULL,
  `created_by` int4 unsigned NOT NULL,
  `changed_by` int4 unsigned ,
  `title` varchar(128) ,
  `url` varchar(256) ,
  `description` text ,
  `categories` varchar(255) ,
  `filename` varchar(255) ,
  `file_store_location` varchar(255) ,
  `file_size` int4 unsigned ,
  `file_mimetype` varchar(255) ,
  `file_md5sum` varchar(32) ,
  `file_version_comment` text ,
  PRIMARY KEY  (`cid`,`infostore_id`,`version_number`),
  INDEX `last_modified`  (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `infostore_property` (
  `cid` int4 unsigned NOT NULL,
  `id` int4 unsigned NOT NULL,
  `name` varchar(128) NOT NULL,
  `namespace` varchar(128) NOT NULL,
  `value` varchar(255) ,
  `language` varchar(128) ,
  `xml` boolean,
  PRIMARY KEY  (`cid`,`id`,`name`,`namespace`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `infostore_lock` (
  `cid` int4 unsigned NOT NULL,
  `id` int4 unsigned NOT NULL,
  `userid` int4 unsigned NOT NULL,
  `entity` int4 unsigned ,
  `timeout` int8 NOT NULL,
  `type` tinyint unsigned NOT NULL,
  `scope` tinyint unsigned NOT NULL,
  `ownerDesc` varchar(128) ,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `lock_null` (
  `cid` int4 unsigned NOT NULL,
  `id` int4 unsigned NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `lock_null_lock` (
  `cid` int4 unsigned NOT NULL,
  `id` int4 unsigned NOT NULL,
  `userid` int4 unsigned ,
  `entity` int4 unsigned ,
  `timeout` int8,
  `type` tinyint unsigned ,
  `scope` tinyint unsigned ,
  `ownerDesc` varchar(128) ,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

