CREATE TABLE `prg_attachment` (
  `cid` int4 unsigned NOT NULL,
  `id` int4 unsigned NOT NULL,
  `created_by` int4 unsigned NOT NULL,
  `creation_date` int8 NOT NULL,
  `file_mimetype` varchar(255) NOT NULL,
  `file_size` int4 unsigned NOT NULL,
  `filename` varchar(255) NOT NULL,
  `attached` int4 unsigned NOT NULL,
  `module` int4 unsigned NOT NULL,
  `rtf_flag` boolean,
  `comment` varchar(255),
  `file_id` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`attached`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `del_attachment` (
  `cid` int4 unsigned NOT NULL,
  `id` int4 unsigned NOT NULL,
  `attached` int4 unsigned NOT NULL,
  `module` int4 unsigned NOT NULL,
  `del_date` int8 NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`attached`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

