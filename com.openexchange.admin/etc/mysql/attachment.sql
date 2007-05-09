CREATE TABLE `prg_attachment` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `creation_date` bigint(20) NOT NULL,
  `file_mimetype` varchar(255) NOT NULL,
  `file_size` int(10) unsigned NOT NULL,
  `filename` varchar(255) NOT NULL,
  `attached` int(10) unsigned NOT NULL,
  `module` int(10) unsigned NOT NULL,
  `rtf_flag` tinyint(1),
  `comment` varchar(255),
  `file_id` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`attached`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;


CREATE TABLE `del_attachment` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `attached` int(10) unsigned NOT NULL,
  `module` int(10) unsigned NOT NULL,
  `del_date` bigint(20) NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`attached`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

