CREATE TABLE `solrCores` (
  `cid` int(10) unsigned NOT NULL,
  `uid` int(10) unsigned NOT NULL,
  `module` int(10) unsigned NOT NULL,
  `store` int(10) unsigned NOT NULL,
  `active` tinyint(1) unsigned NOT NULL,  
  `server` varchar(32) DEFAULT NULL,  
  PRIMARY KEY  (`cid`,`uid`,`module`),
  KEY `cidserver` (`cid`, `server`),
  KEY `server` (`server`),
  KEY `store` (`store`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `solrCoreStores` (
  `id` int(10) unsigned NOT NULL,
  `uri` varchar(255) NOT NULL,
  `maxCores` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*
CREATE TABLE `solrIndexFiles` (
  `cid` int(10) unsigned NOT NULL,
  `uid` int(10) unsigned NOT NULL,
  `module` int(10) unsigned NOT NULL,
  `indexFile` varchar(32) NOT NULL,
  PRIMARY KEY  (`cid`,`uid`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `solrServers` (
  `id` int(10) unsigned NOT NULL,
  `serverUrl` varchar(32) NOT NULL,
  `maxIndices` int(10) unsigned NOT NULL,
  `socketTimeout` int(10) unsigned DEFAULT '0',
  `connectionTimeout` int(10) unsigned DEFAULT '0',
  `maxConnections` int(10) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `url` (`serverUrl`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
*/