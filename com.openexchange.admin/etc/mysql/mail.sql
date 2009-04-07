CREATE TABLE `user_mail_account` (
    id INT4 unsigned NOT NULL,
    cid INT4 unsigned NOT NULL,
    user INT4 unsigned NOT NULL,
    name VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    url VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    login varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    password VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
    primary_addr VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    default_flag TINYINT unsigned NOT NULL default 0,
    spam_handler VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    trash VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    sent VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    drafts VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    spam VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    confirmed_spam VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    confirmed_ham VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    PRIMARY KEY  (cid, id, user),
    INDEX (cid, user),
    FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `user_transport_account` (
    id INT4 unsigned NOT NULL,
    cid INT4 unsigned NOT NULL,
    user INT4 unsigned NOT NULL,
    name VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    url VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    login varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    password VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
    send_addr VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    default_flag TINYINT unsigned NOT NULL default 0,
    PRIMARY KEY  (cid, id, user),
    INDEX (cid, user),
    FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;