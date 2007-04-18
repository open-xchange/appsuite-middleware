#@(#) settings.sql consistency


ALTER TABLE user_setting
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY user_id INT4 UNSIGNED NOT NULL,
    MODIFY path_id INT4 UNSIGNED NOT NULL,
    MODIFY value TEXT,
    ENGINE = InnoDB;
    
ALTER TABLE user_configuration
	MODIFY cid INT4 UNSIGNED NOT NULL,
	MODIFY user INT4 UNSIGNED NOT NULL,
	MODIFY permissions INT4 UNSIGNED NOT NULL,
	ADD FOREIGN KEY (cid, user) REFERENCES user (cid, id),
	ENGINE = InnoDB;

ALTER TABLE user_setting_mail
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY user INT4 UNSIGNED NOT NULL,
    MODIFY bits INTEGER(32) UNSIGNED DEFAULT 0,
    MODIFY send_addr VARCHAR(256) character set utf8 collate utf8_unicode_ci NOT NULL,
    MODIFY reply_to_addr VARCHAR(256) character set utf8 collate utf8_unicode_ci default NULL,
    MODIFY msg_format TINYINT(4) UNSIGNED DEFAULT 1,
    MODIFY display_msg_headers varchar(256) character set utf8 collate utf8_unicode_ci default NULL,
    MODIFY auto_linebreak INT4 UNSIGNED DEFAULT 80,
    MODIFY std_trash VARCHAR(128) character set utf8 collate utf8_unicode_ci NOT NULL,
    MODIFY std_sent VARCHAR(128) character set utf8 collate utf8_unicode_ci NOT NULL,
    MODIFY std_drafts VARCHAR(128) character set utf8 collate utf8_unicode_ci NOT NULL,
    MODIFY std_spam VARCHAR(128) character set utf8 collate utf8_unicode_ci NOT NULL,
    MODIFY confirmed_spam VARCHAR(128) character set utf8 collate utf8_unicode_ci NOT NULL,
    MODIFY confirmed_ham VARCHAR(128) character set utf8 collate utf8_unicode_ci NOT NULL,
    MODIFY upload_quota INT4 UNSIGNED DEFAULT 0,
    MODIFY upload_quota_per_file INT4 UNSIGNED DEFAULT 0,
    ADD FOREIGN KEY (cid, user) REFERENCES user (cid, id),
    ENGINE = InnoDB;
    
ALTER TABLE user_setting_mail_signature
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY user INT4 UNSIGNED NOT NULL,
    MODIFY id VARCHAR(64) character set utf8 collate utf8_unicode_ci NOT NULL,
    MODIFY signature VARCHAR(1024) character set utf8 collate utf8_unicode_ci NOT NULL,
    ADD INDEX (cid, user),
    ADD FOREIGN KEY (cid, user) REFERENCES user_setting_mail (cid, user),
    ENGINE = InnoDB;
    
ALTER TABLE user_setting_spellcheck
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY user INT4 UNSIGNED NOT NULL,
    MODIFY user_dic text character set utf8 collate utf8_unicode_ci default NULL,
    ADD FOREIGN KEY (cid, user) REFERENCES user (cid, id),
    ENGINE = InnoDB;
    
ALTER TABLE user_setting_admin
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY user INT4 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (cid, user) REFERENCES user (cid, id),
    ENGINE = InnoDB;
