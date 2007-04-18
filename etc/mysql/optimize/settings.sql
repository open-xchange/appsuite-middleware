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
