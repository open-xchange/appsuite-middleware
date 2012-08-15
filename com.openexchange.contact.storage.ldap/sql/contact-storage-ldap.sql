CREATE TABLE ldapIds (
 cid INT4 unsigned NOT NULL,
 fid INT4 unsigned NOT NULL,
 contact INT4 unsigned NOT NULL,
 ldapId VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 PRIMARY KEY (cid, fid, contact),
 INDEX `ldapId` (cid, fid, ldapId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
