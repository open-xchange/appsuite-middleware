CREATE TABLE `solrCores` (
  `cid` int(10) unsigned NOT NULL,
  `uid` int(10) unsigned NOT NULL,
  `module` int(10) unsigned NOT NULL,
  `active` tinyint(1) unsigned NOT NULL,
  `core` varchar(32) DEFAULT NULL,
  `server` int(10) unsigned DEFAULT NULL,  
  PRIMARY KEY  (`cid`,`uid`,`module`),
  KEY `server` (`server`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `solrIndexFiles` (
  `cid` int(10) unsigned NOT NULL,
  `uid` int(10) unsigned NOT NULL,
  `module` int(10) unsigned NOT NULL,
  `indexFile` varchar(32) NOT NULL,
  PRIMARY KEY  (`cid`,`uid`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `solrServers` (
  `id` int(10) unsigned NOT NULL,
  `serverUrl` varchar(32) NOT NULL,
  `maxIndices` int(10) unsigned NOT NULL,
  `socketTimeout` int(10) unsigned DEFAULT '0',
  `connectionTimeout` int(10) unsigned DEFAULT '0',
  `maxConnections` int(10) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `url` (`serverUrl`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8