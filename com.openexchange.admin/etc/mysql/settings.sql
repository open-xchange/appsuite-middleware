
CREATE TABLE user_configuration (
	cid integer,
	user integer,
	permissions integer
);

CREATE TABLE user_setting_mail (
    cid integer,
    user integer,
    bits integer,
    send_addr text,
    reply_to_addr  text,
    msg_format integer,
    display_msg_headers text,
    auto_linebreak integer,
    std_trash text,
    std_sent text,
    std_drafts text,
    std_spam text,
    confirmed_spam text,
    confirmed_ham text,
    upload_quota integer,
    upload_quota_per_file integer
);

CREATE TABLE user_setting_mail_signature (
    cid integer,
    user integer,
    id text,
    signature text
);

CREATE TABLE user_setting_spellcheck (
    cid integer,
    user integer,
    user_dic text
);

CREATE TABLE user_setting_admin (
    cid integer,
    user integer
);

CREATE TABLE user_setting (
    cid INTEGER,
    user_id INTEGER,
    path_id INTEGER,
    value text
);
#@(#) settings.sql optimizations


ALTER TABLE user_setting
    MODIFY cid INT4 UNSIGNED,
    MODIFY user_id INT4 UNSIGNED,
    MODIFY path_id INT4 UNSIGNED,
    MODIFY value TEXT,
    ADD PRIMARY KEY (cid, user_id, path_id),
    DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
    
ALTER TABLE user_configuration
	MODIFY cid INT4 UNSIGNED,
	MODIFY user INT4 UNSIGNED,
	MODIFY permissions INT4 UNSIGNED,
	ADD PRIMARY KEY (cid, user),
	DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
	
ALTER TABLE user_setting_mail
    MODIFY cid INT4 UNSIGNED,
    MODIFY user INT4 UNSIGNED,
    MODIFY bits INTEGER(32) UNSIGNED,
    MODIFY send_addr VARCHAR(256),
    MODIFY reply_to_addr VARCHAR(256),
    MODIFY msg_format TINYINT(4) UNSIGNED,
    MODIFY display_msg_headers varchar(256),
    MODIFY auto_linebreak INT4 UNSIGNED,
    MODIFY std_trash VARCHAR(128),
    MODIFY std_sent VARCHAR(128),
    MODIFY std_drafts VARCHAR(128),
    MODIFY std_spam VARCHAR(128),
    MODIFY confirmed_spam VARCHAR(128),
    MODIFY confirmed_ham VARCHAR(128),
    MODIFY upload_quota INT4 UNSIGNED,
    MODIFY upload_quota_per_file INT4 UNSIGNED,
    ADD PRIMARY KEY (cid, user),
    DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
    
ALTER TABLE user_setting_mail_signature
    MODIFY cid INT4 UNSIGNED,
    MODIFY user INT4 UNSIGNED,
    MODIFY id VARCHAR(64),
    MODIFY signature VARCHAR(1024),
    ADD PRIMARY KEY (cid, user, id),
    DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
    
ALTER TABLE user_setting_spellcheck
    MODIFY cid INT4 UNSIGNED,
    MODIFY user INT4 UNSIGNED,
    MODIFY user_dic text,
    ADD PRIMARY KEY (cid, user),
    DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
    
ALTER TABLE user_setting_admin
    MODIFY cid INT4 UNSIGNED,
    MODIFY user INT4 UNSIGNED,
    ADD PRIMARY KEY (cid, user),
    DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
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
    MODIFY upload_quota INT4 DEFAULT -1,
    MODIFY upload_quota_per_file INT4 DEFAULT -1,
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
