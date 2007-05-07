#@(#) ldap2sql.sql #@(#) ldap2sql.sql optimizations

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

#@(#) ldap2sql.sql consistency

ALTER TABLE groups
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY identifier VARCHAR(128) NOT NULL,
    MODIFY displayName VARCHAR(128) NOT NULL,
    MODIFY lastModified INT8 NOT NULL,
    MODIFY gidNumber INT4 UNSIGNED NOT NULL,
	ENGINE=InnoDB;

ALTER TABLE del_groups
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY identifier VARCHAR(128) NOT NULL,
    MODIFY displayName VARCHAR(128) NOT NULL,
    MODIFY lastModified INT8 NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE user
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY mail VARCHAR(256) NOT NULL,
    MODIFY mailEnabled boolean NOT NULL,
    MODIFY preferredLanguage VARCHAR(10) NOT NULL,
    MODIFY shadowLastChange INTEGER NOT NULL,
    MODIFY timeZone VARCHAR(128) NOT NULL,
    MODIFY contactId INT4 UNSIGNED NOT NULL,
	MODIFY passwordMech VARCHAR(32) NOT NULL,
    MODIFY uidNumber INT4 UNSIGNED NOT NULL,
    MODIFY gidNumber INT4 UNSIGNED NOT NULL,
    MODIFY homeDirectory VARCHAR(128) NOT NULL,
    MODIFY loginShell VARCHAR(128) NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE del_user
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY mail VARCHAR(256) NOT NULL,
    MODIFY mailEnabled boolean NOT NULL,
    MODIFY preferredLanguage VARCHAR(10) NOT NULL,
    MODIFY shadowLastChange INTEGER NOT NULL,
    MODIFY timeZone VARCHAR(128) NOT NULL,
    MODIFY contactId INT4 UNSIGNED NOT NULL,
    MODIFY lastModified INT8 NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE groups_member
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY member INT4 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (cid, id) REFERENCES groups(cid, id),
    ADD FOREIGN KEY (cid, member) REFERENCES user(cid, id),
    ENGINE=InnoDB;

ALTER TABLE login2user
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY uid VARCHAR(128) NOT NULL,
    ADD FOREIGN KEY (cid, id) REFERENCES user(cid, id),
    ENGINE=InnoDB;

ALTER TABLE user_attribute
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY name VARCHAR(128) NOT NULL,
    MODIFY value VARCHAR(128) NOT NULL,
    ADD FOREIGN KEY (cid, id) REFERENCES user(cid, id),
    ENGINE=InnoDB;

ALTER TABLE resource_group
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY identifier VARCHAR(128) NOT NULL,
    MODIFY displayName VARCHAR(128) NOT NULL,
    MODIFY available boolean NOT NULL,
    MODIFY lastModified INT8 NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE del_resource_group
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY identifier VARCHAR(128) NOT NULL,
    MODIFY displayName VARCHAR(128) NOT NULL,
    MODIFY available boolean NOT NULL,
    MODIFY lastModified INT8 NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE resource
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY identifier VARCHAR(128) NOT NULL,
    MODIFY displayName VARCHAR(128) NOT NULL,
    MODIFY available boolean NOT NULL,
    MODIFY lastModified INT8 NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE del_resource
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY identifier VARCHAR(128) NOT NULL,
    MODIFY displayName VARCHAR(128) NOT NULL,
    MODIFY available boolean NOT NULL,
    MODIFY lastModified INT8 NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE resource_group_member
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY member INT4 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (cid, id) REFERENCES resource_group(cid, id),
    ADD FOREIGN KEY (cid, member) REFERENCES resource(cid, id),
    ENGINE=InnoDB;

