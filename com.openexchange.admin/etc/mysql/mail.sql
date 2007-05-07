#@(#) mail.sql optimizations


CREATE TABLE mail_domains (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
	domainName VARCHAR(128),
	smtpSenderRule VARCHAR(128),
	restriction VARCHAR(64),
    PRIMARY KEY (cid, id),
    INDEX (domainName)
);

CREATE TABLE user_mail_restrictions (
	cid INT4 UNSIGNED,
	id INT4 UNSIGNED,
	className VARCHAR(64),
	address VARCHAR(255),
	INDEX(className, address)
);

CREATE TABLE group_address_mappings (
	cid INT4 UNSIGNED,
	id INT4 UNSIGNED,
	address VARCHAR(255),
	INDEX(cid,address)
);

CREATE TABLE resource_address_mappings (
	cid INT4 UNSIGNED,
	id INT4 UNSIGNED,
	destination VARCHAR(255),
	INDEX(cid)
);

CREATE TABLE address_mappings (
	cid INT4 UNSIGNED,
	id INT4 UNSIGNED,
	address VARCHAR(255),
	destination VARCHAR(255),
    PRIMARY KEY (cid, id),
	INDEX(address)
);
#@(#) mail.sql consistency


ALTER TABLE mail_domains
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
	MODIFY domainName VARCHAR(128) UNIQUE NOT NULL,
	MODIFY smtpSenderRule VARCHAR(128) NOT NULL,
	MODIFY restriction VARCHAR(64) NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE user_mail_restrictions
	MODIFY cid INT4 UNSIGNED NOT NULL,
	MODIFY id INT4 UNSIGNED NOT NULL,
	MODIFY className VARCHAR(64) NOT NULL,
	MODIFY address VARCHAR(255) UNIQUE NOT NULL,
    ADD FOREIGN KEY (cid, id) REFERENCES user(cid, id),
	ENGINE=InnoDB;

ALTER TABLE group_address_mappings
	MODIFY cid INT4 UNSIGNED NOT NULL,
	MODIFY id INT4 UNSIGNED NOT NULL,
	MODIFY address VARCHAR(255) UNIQUE NOT NULL,
    ADD FOREIGN KEY (cid, id) REFERENCES groups(cid, id),
	ENGINE=InnoDB;

ALTER TABLE resource_address_mappings
	MODIFY cid INT4 UNSIGNED NOT NULL,
	MODIFY id INT4 UNSIGNED NOT NULL,
	MODIFY destination VARCHAR(255) NOT NULL,
    ADD FOREIGN KEY (cid, id) REFERENCES resource(cid, id),
	ENGINE=InnoDB;

ALTER TABLE address_mappings
	MODIFY cid INT4 UNSIGNED NOT NULL,
	MODIFY id INT4 UNSIGNED NOT NULL,
	MODIFY address VARCHAR(255) UNIQUE NOT NULL,
	MODIFY destination VARCHAR(255) NOT NULL,
	ENGINE=InnoDB;
