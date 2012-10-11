#@(#) Tables to store all those user configurations.

CREATE TABLE user_configuration (
    cid INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    permissions INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid, user),
    FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
    
CREATE TABLE user_setting_mail (
    cid INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    bits INT4 UNSIGNED DEFAULT 0,
    send_addr VARCHAR(256) NOT NULL,
    reply_to_addr VARCHAR(256) DEFAULT NULL,
    msg_format TINYINT(4) UNSIGNED DEFAULT 1,
    display_msg_headers VARCHAR(256) DEFAULT NULL,
    auto_linebreak INT4 UNSIGNED DEFAULT 80,
    std_trash VARCHAR(128) NOT NULL,
    std_sent VARCHAR(128) NOT NULL,
    std_drafts VARCHAR(128) NOT NULL,
    std_spam VARCHAR(128) NOT NULL,
    confirmed_spam VARCHAR(128) NOT NULL,
    confirmed_ham VARCHAR(128) NOT NULL,
    upload_quota INT4 DEFAULT -1,
    upload_quota_per_file INT4 DEFAULT -1,
    PRIMARY KEY (cid, user),
    FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE user_setting_mail_signature (
    cid INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    id VARCHAR(64) NOT NULL,
    signature VARCHAR(1024) NOT NULL,
    PRIMARY KEY (cid, user, id),
    FOREIGN KEY (cid, user) REFERENCES user_setting_mail (cid, user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE user_setting_spellcheck (
    cid INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    user_dic TEXT,
    PRIMARY KEY (cid, user),
    FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE user_setting_admin (
    cid INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid, user),
    FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE user_setting (
    cid INT4 UNSIGNED NOT NULL,
    user_id INT4 UNSIGNED NOT NULL,
    path_id INT4 UNSIGNED NOT NULL,
    value TEXT,
    PRIMARY KEY (cid, user_id, path_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE user_setting_server (
    cid INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    contact_collect_folder INT4 UNSIGNED,
    contact_collect_enabled BOOL,
    defaultStatusPrivate INT4 UNSIGNED DEFAULT 0,
    defaultStatusPublic INT4 UNSIGNED DEFAULT 0,
    contactCollectOnMailTransport BOOL DEFAULT TRUE,
    contactCollectOnMailAccess BOOL DEFAULT TRUE,
    folderTree INT4,
    FOREIGN KEY(cid, user) REFERENCES user(cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
