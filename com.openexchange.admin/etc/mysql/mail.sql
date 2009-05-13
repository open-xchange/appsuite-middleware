CREATE TABLE user_mail_account (
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
 unified_inbox TINYINT unsigned default 0,
 trash_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 sent_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 drafts_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 spam_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 confirmed_spam_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 confirmed_ham_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 PRIMARY KEY  (cid, id, user),
 INDEX (cid, user),
 FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
 
CREATE TABLE user_mail_account_properties (
 id INT4 unsigned NOT NULL,
 cid INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 name VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 value VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 PRIMARY KEY  (cid, id, user, name),
 FOREIGN KEY (cid, id, user) REFERENCES user_mail_account (cid, id, user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE user_transport_account (
 id INT4 unsigned NOT NULL,
 cid INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 name VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 url VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 login varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 password VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
 send_addr VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 default_flag TINYINT unsigned NOT NULL default 0,
 unified_inbox TINYINT unsigned default 0,
 PRIMARY KEY  (cid, id, user),
 INDEX (cid, user),
 FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE user_transport_account_properties (
 id INT4 unsigned NOT NULL,
 cid INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 name VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 value VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 PRIMARY KEY  (cid, id, user, name),
 FOREIGN KEY (cid, id, user) REFERENCES user_transport_account (cid, id, user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE pop3_storage_ids (
 cid INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 id INT4 unsigned NOT NULL,
 uidl VARCHAR(70) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 uid VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 PRIMARY KEY (cid, user, id, uidl),
 FOREIGN KEY (cid, user) REFERENCES user (cid, id),
 FOREIGN KEY (cid, user, id) REFERENCES user_mail_account (cid, user, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE pop3_storage_deleted (
 cid INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 id INT4 unsigned NOT NULL,
 uidl VARCHAR(70) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 PRIMARY KEY (cid, user, id, uidl),
 FOREIGN KEY (cid, user) REFERENCES user (cid, id),
 FOREIGN KEY (cid, user, id) REFERENCES user_mail_account (cid, user, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
