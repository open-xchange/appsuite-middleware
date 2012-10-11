#@(#) Tables for the virtual folder.

CREATE TABLE virtualTree (
 cid INT4 unsigned NOT NULL,
 tree INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 parentId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 name VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 lastModified BIGINT(64) DEFAULT NULL,
 modifiedBy INT4 unsigned DEFAULT NULL,
 shadow VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 sortNum INT4 unsigned DEFAULT NULL,
 PRIMARY KEY (cid, tree, user, folderId),
 INDEX (cid, tree, user, parentId),
 INDEX (cid, tree, user, shadow),
 INDEX (cid, user),
 INDEX (cid, modifiedBy)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE virtualPermission (
 cid INT4 unsigned NOT NULL,
 tree INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 entity INT4 unsigned NOT NULL,
 fp tinyint(3) unsigned NOT NULL,
 orp tinyint(3) unsigned NOT NULL,
 owp tinyint(3) unsigned NOT NULL,
 odp tinyint(3) unsigned NOT NULL,
 adminFlag tinyint(3) unsigned NOT NULL,
 groupFlag tinyint(3) unsigned NOT NULL,
 system tinyint(3) unsigned NOT NULL default '0',
 PRIMARY KEY (cid, tree, user, folderId, entity),
 INDEX (cid, tree, user, folderId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE virtualSubscription (
 cid INT4 unsigned NOT NULL,
 tree INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 subscribed tinyint(3) unsigned NOT NULL,
 PRIMARY KEY (cid, tree, user, folderId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE virtualBackupTree (
 cid INT4 unsigned NOT NULL,
 tree INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 parentId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 name VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 lastModified BIGINT(64) DEFAULT NULL,
 modifiedBy INT4 unsigned DEFAULT NULL,
 shadow VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 sortNum INT4 unsigned DEFAULT NULL,
 PRIMARY KEY (cid, tree, user, folderId),
 INDEX (cid, tree, user, parentId),
 INDEX (cid, tree, user, shadow),
 INDEX (cid, user),
 INDEX (cid, modifiedBy)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE virtualBackupPermission (
 cid INT4 unsigned NOT NULL,
 tree INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 entity INT4 unsigned NOT NULL,
 fp tinyint(3) unsigned NOT NULL,
 orp tinyint(3) unsigned NOT NULL,
 owp tinyint(3) unsigned NOT NULL,
 odp tinyint(3) unsigned NOT NULL,
 adminFlag tinyint(3) unsigned NOT NULL,
 groupFlag tinyint(3) unsigned NOT NULL,
 system tinyint(3) unsigned NOT NULL default '0',
 PRIMARY KEY (cid, tree, user, folderId, entity),
 INDEX (cid, tree, user, folderId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE virtualBackupSubscription (
 cid INT4 unsigned NOT NULL,
 tree INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 subscribed tinyint(3) unsigned NOT NULL,
 PRIMARY KEY (cid, tree, user, folderId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

