CREATE TABLE `genconf_attributes_strings` (
   `cid` int(10) unsigned NOT NULL,
   `id` int(10) unsigned NOT NULL,
   `name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
   `value` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
   KEY (`cid`,`id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `genconf_attributes_bools` (
   `cid` int(10) unsigned NOT NULL,
   `id` int(10) unsigned NOT NULL,
   `name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
   `value` tinyint(1) COLLATE utf8_unicode_ci DEFAULT NULL,
   KEY (`cid`,`id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `sequence_genconf` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci