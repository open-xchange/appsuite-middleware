# Tables for OAuth provider

CREATE TABLE `oauthServiceProvider` (
  `id` int(10) unsigned NOT NULL,
  `requestTokenUrl` varchar(255) NOT NULL,
  `userAuthorizationUrl` varchar(255) NOT NULL,
  `accessTokenURL` varchar(255) DEFAULT NULL,
  PRIMARY KEY  (`id`),
  KEY `nameIndex` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauthConsumer` (
  `id` int(10) unsigned NOT NULL,
  `providerId` int(10) unsigned NOT NULL,
  `key` varchar(255) NOT NULL,
  `secret` varchar(255) NOT NULL,
  `callbackUrl` varchar(255) DEFAULT NULL,
  `name` varchar(127) DEFAULT NULL,
  PRIMARY KEY  (`id`,`providerId`),
  KEY `providerIndex` (`providerId`),
  KEY `keyIndex` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauthAccessor` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `consumerId` int(10) unsigned NOT NULL,
  `providerId` int(10) unsigned NOT NULL,
  `requestToken` varchar(255) NOT NULL,
  `accessToken` varchar(255) NOT NULL,
  `tokenSecret` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`user`,`consumerId`,`providerId`),
  KEY `consumerIndex` (`cid`,`consumerId`,`providerId`),
  KEY `providerIndex` (`cid`,`providerId`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
