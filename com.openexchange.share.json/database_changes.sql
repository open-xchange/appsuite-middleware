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
