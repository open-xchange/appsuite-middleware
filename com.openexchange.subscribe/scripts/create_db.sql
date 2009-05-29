DROP TABLE IF EXISTS subscriptions;

CREATE TABLE subscriptions (
id INT(10) UNSIGNED NOT NULL, 
cid INT(10) UNSIGNED NOT NULL, 
user_id INT(10) UNSIGNED NOT NULL, 
configuration_id INT(10) UNSIGNED NOT NULL, 
source_id VARCHAR(255) NOT NULL, 
folder_id VARCHAR(255) NOT NULL, 
last_update BIGINT(20) UNSIGNED NOT NULL, 
PRIMARY KEY (id, cid), 
FOREIGN KEY(cid, user_id) REFERENCES user(cid, id))
ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS sequence_subscriptions;

CREATE TABLE sequence_subscriptions (
cid int(10) unsigned NOT NULL,
id int(10) unsigned NOT NULL,
PRIMARY KEY  (cid))
ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;