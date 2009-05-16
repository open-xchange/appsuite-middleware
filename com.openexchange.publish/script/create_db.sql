DROP TABLE IF EXISTS publications;

CREATE TABLE publications (
id INT(10) UNSIGNED NOT NULL, 
cid INT(10) UNSIGNED NOT NULL, 
user_id INT(10) UNSIGNED NOT NULL, 
entity INT(10) UNSIGNED NOT NULL, 
module VARCHAR(255) NOT NULL, 
configuration_id INT(10) UNSIGNED NOT NULL, 
target_id VARCHAR(255) NOT NULL, 
PRIMARY KEY (id), 
FOREIGN KEY(cid, user_id) REFERENCES user(cid, id)) 
ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS sequence_publications;

CREATE TABLE sequence_publications (
cid int(10) unsigned NOT NULL,
id int(10) unsigned NOT NULL,
PRIMARY KEY  (cid))
ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;