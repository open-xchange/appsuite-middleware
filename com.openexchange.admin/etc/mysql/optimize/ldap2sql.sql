#@(#) ldap2sql.sql optimizations

CREATE TABLE groups (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    identifier VARCHAR(128),
    displayName VARCHAR(128),
    lastModified INT8,
    gidNumber INT4 UNSIGNED,
    PRIMARY KEY (cid, id)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE INDEX groups_identifier_idx ON groups(identifier(32));

CREATE TABLE del_groups (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    identifier VARCHAR(128),
    displayName VARCHAR(128),
    lastModified INT8,
    PRIMARY KEY (cid, id)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE user (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    imapServer VARCHAR(128),
    imapLogin VARCHAR(128),
    mail VARCHAR(256),
    mailDomain VARCHAR(128),
    mailEnabled boolean,
    preferredLanguage VARCHAR(10),
    shadowLastChange INTEGER,
    smtpServer VARCHAR(128),
    timeZone VARCHAR(128),
    userPassword VARCHAR(128),
    contactId INT4 UNSIGNED,
	passwordMech VARCHAR(32),
    uidNumber INT4 UNSIGNED,
    gidNumber INT4 UNSIGNED,
    homeDirectory VARCHAR(128),
    loginShell VARCHAR(128),
    PRIMARY KEY (cid, id),
    INDEX (mail)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_user (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    imapServer VARCHAR(128),
    imapLogin VARCHAR(128),
    mail VARCHAR(256),
    mailDomain VARCHAR(128),
    mailEnabled boolean,
    preferredLanguage VARCHAR(10),
    shadowLastChange INTEGER,
    smtpServer VARCHAR(128),
    timeZone VARCHAR(128),
    contactId INT4 UNSIGNED,
    lastModified INT8,
    userPassword VARCHAR(128),
    PRIMARY KEY (cid, id)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE groups_member (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    member INT4 UNSIGNED,
    PRIMARY KEY (cid, id, member)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE INDEX groups_member_cid_id_idx ON groups_member(cid, id);

CREATE TABLE login2user (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    uid VARCHAR(128),
    PRIMARY KEY (cid, uid)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE user_attribute (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    name VARCHAR(128),
    value VARCHAR(128),
    INDEX (cid,name,value)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE resource_group (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    identifier VARCHAR(128),
    displayName VARCHAR(128),
    available boolean,
    description VARCHAR(255),
    lastModified INT8,
    PRIMARY KEY (cid, id)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_resource_group (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    identifier VARCHAR(128),
    displayName VARCHAR(128),
    available boolean,
    description VARCHAR(255),
    lastModified INT8,
    PRIMARY KEY (cid, id)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE INDEX resource_group_identifier_idx ON resource_group(identifier(32));

CREATE TABLE resource (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    identifier VARCHAR(128),
    displayName VARCHAR(128),
    mail VARCHAR(256),
    available boolean,
    description VARCHAR(255),
    lastModified INT8,
    PRIMARY KEY (cid, id)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_resource (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    identifier VARCHAR(128),
    displayName VARCHAR(128),
    mail VARCHAR(256),
    available boolean,
    description VARCHAR(255),
    lastModified INT8,
    PRIMARY KEY (cid, id)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE INDEX resource_identifier_idx ON resource(identifier(32));

CREATE TABLE resource_group_member (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
    member INT4 UNSIGNED,
    PRIMARY KEY (cid, id, member)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE INDEX resource_group_member_cid_id_idx ON resource_group_member(cid, id);

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

CREATE TABLE address_mappings (
	cid INT4 UNSIGNED,
	id INT4 UNSIGNED,
	address VARCHAR(255),
	destination VARCHAR(255),
    PRIMARY KEY (cid, id),
	INDEX(address)
);