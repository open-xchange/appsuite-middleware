ALTER TABLE oxfolder_permissions ADD COLUMN `guest_flag` tinyint(3) unsigned NOT NULL DEFAULT 0;

CREATE TABLE `guest` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `createdBy` int(10) unsigned NOT NULL,
  `displayName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mail` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
  `userPassword` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  `passwordMech` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `timeZone` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `preferredLanguage` varchar(10) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`cid`,`id`),
  KEY `mailIndex` (`cid`,`mail`(255)),
  KEY `createdByIndex` (`cid`,`createdBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci

CREATE TABLE `share` (
  `token` binary(16) NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `module` tinyint(3) unsigned NOT NULL,
  `folder` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `item` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created` bigint(64) NOT NULL,
  `createdBy` int(10) unsigned NOT NULL,
  `lastModified` bigint(64) NOT NULL,
  `modifiedBy` int(10) unsigned NOT NULL,
  `expires` bigint(64) DEFAULT NULL,
  `guest` int(10) unsigned NOT NULL,
  `auth` tinyint(3) unsigned NOT NULL,
  `displayName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`cid`,`token`),
  KEY `createdByIndex` (`cid`,`createdBy`),
  KEY `guestIndex` (`cid`,`guest`),
  KEY `folderIndex` (`cid`,`folder`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci

