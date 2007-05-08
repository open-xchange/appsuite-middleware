
# 2007-05-08, Carsten Hoeger, added version table
CREATE TABLE version (
	version INT4 UNSIGNED NOT NULL,
	locked BOOLEAN NOT NULL,
	gw_compatible BOOLEAN NOT NULL,
	admin_compatible BOOLEAN NOT NULL,
	server VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL
) ENGINE = InnoDB;

# 2007-02-05, Carsten Hoeger, initial entries
ALTER TABLE user_setting_mail ADD COLUMN confirmed_ham VARCHAR(128) NOT NULL;
ALTER TABLE user_setting_mail ADD COLUMN confirmed_spam VARCHAR(128) NOT NULL;
ALTER TABLE groups ADD COLUMN gidNumber INT4 UNSIGNED NOT NULL;
ALTER TABLE user ADD COLUMN passwordMech VARCHAR(32) NOT NULL;
ALTER TABLE user ADD COLUMN uidNumber INT4 UNSIGNED NOT NULL;
ALTER TABLE user ADD COLUMN gidNumber INT4 UNSIGNED NOT NULL;
ALTER TABLE user ADD COLUMN homeDirectory VARCHAR(128) NOT NULL;
ALTER TABLE user ADD COLUMN loginShell VARCHAR(128) NOT NULL;
