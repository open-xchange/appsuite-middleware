# Tables for OAuth provider

CREATE TABLE `oauthServiceProvider` (
  `id` int(10) unsigned NOT NULL,
  `requestTokenUrl` varchar(255) NOT NULL,
  `userAuthorizationUrl` varchar(255) NOT NULL,
  `accessTokenURL` varchar(255) DEFAULT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauthConsumer` (
  `id` int(10) unsigned NOT NULL,
  `providerId` int(10) unsigned NOT NULL,
  `key` varchar(255) NOT NULL,
  `secret` varchar(255) NOT NULL,
  `callbackUrl` varchar(255) DEFAULT NULL,
  `name` varchar(127) DEFAULT NULL,
  PRIMARY KEY  (`id`),
  KEY `providerIndex` (`providerId`),
  KEY `keyIndex` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauthConsumerProperty` (
  `id` int(10) unsigned NOT NULL,
  `name` varchar(32) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauth2Client` (
  `id` int(10) unsigned NOT NULL,
  `providerId` int(10) unsigned NOT NULL,
  `key` varchar(255) NOT NULL,
  `secret` varchar(255) NOT NULL,
  `callbackUrl` varchar(255) DEFAULT NULL,
  `name` varchar(127) DEFAULT NULL,
  PRIMARY KEY  (`id`),
  KEY `providerIndex` (`providerId`),
  KEY `keyIndex` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauth2ClientProperty` (
  `id` int(10) unsigned NOT NULL,
  `name` varchar(32) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauthNonce` (
  `nonce` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauthAccessor` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `consumerId` int(10) unsigned NOT NULL,
  `providerId` int(10) unsigned NOT NULL,
  `requestToken` varchar(255) DEFAULT NULL,
  `accessToken` varchar(255) DEFAULT NULL,
  `tokenSecret` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`user`,`consumerId`),
  KEY `userIndex` (`cid`,`user`),
  KEY `consumerIndex` (`consumerId`,`providerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauthAccessorProperty` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `consumerId` int(10) unsigned NOT NULL,
  `name` varchar(32) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`user`,`consumerId`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauth2Accessor` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `clientId` int(10) unsigned NOT NULL,
  `providerId` int(10) unsigned NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `refreshToken` varchar(255) DEFAULT NULL,
  `accessToken` varchar(255) DEFAULT NULL,
  `expiresIn` varchar(255) DEFAULT NULL,
  `tokenType` varchar(255) DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  PRIMARY KEY  (`cid`,`user`,`clientId`),
  KEY `userIndex` (`cid`,`user`),
  KEY `consumerIndex` (`clientId`,`providerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `oauth2AccessorProperty` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `clientId` int(10) unsigned NOT NULL,
  `name` varchar(32) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`user`,`clientId`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
