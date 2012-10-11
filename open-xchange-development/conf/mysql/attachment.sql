#@(#) Tables for attachments

CREATE TABLE `prg_attachment` (
  `cid` INT4 UNSIGNED NOT NULL,
  `id` INT4 UNSIGNED NOT NULL,
  `created_by` INT4 UNSIGNED NOT NULL,
  `creation_date` INT8 NOT NULL,
  `file_mimetype` varchar(255) NOT NULL,
  `file_size` INT4 UNSIGNED NOT NULL,
  `filename` varchar(255) NOT NULL,
  `attached` INT4 UNSIGNED NOT NULL,
  `module` INT4 UNSIGNED NOT NULL,
  `rtf_flag` boolean,
  `comment` varchar(255),
  `file_id` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`attached`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `del_attachment` (
  `cid` INT4 UNSIGNED NOT NULL,
  `id` INT4 UNSIGNED NOT NULL,
  `attached` INT4 UNSIGNED NOT NULL,
  `module` INT4 UNSIGNED NOT NULL,
  `del_date` INT8 NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`attached`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
