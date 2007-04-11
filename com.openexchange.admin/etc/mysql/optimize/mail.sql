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
