#@(#) Tables for the calendar

CREATE TABLE prg_dates (
    creating_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_from INT4 UNSIGNED NOT NULL,
    changing_date INT8 NOT NULL,
    changed_from INT4 UNSIGNED NOT NULL,
    fid INT4 UNSIGNED NOT NULL,
    pflag INT4 UNSIGNED NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    timestampfield01 DATETIME NOT NULL,
    timestampfield02 DATETIME NOT NULL,
    timezone VARCHAR(64) NOT NULL,
    intfield01 INT4 UNSIGNED NOT NULL,
    intfield02 INT4 UNSIGNED,
    intfield03 INT4 UNSIGNED,
    intfield04 INT4 UNSIGNED,
    intfield05 INT4 UNSIGNED,
    intfield06 INT4 UNSIGNED NOT NULL,
    intfield07 INT4 UNSIGNED,
    intfield08 INT4 UNSIGNED,
    field01 VARCHAR(255),
    field02 VARCHAR(255),
    field04 TEXT,
    field06 VARCHAR(64),
    field07 TEXT,
    field08 TEXT,
    field09 VARCHAR(255),
    uid VARCHAR(1024),
    organizer VARCHAR(255),
    sequence INT4 UNSIGNED,
    organizerId INT4 UNSIGNED,
    principal VARCHAR(255),
    principalId INT4 UNSIGNED,
    filename VARCHAR(255),
    PRIMARY KEY (cid, intfield01),
    INDEX (cid, intfield02),
    INDEX (cid, timestampfield01),
    INDEX (cid, timestampfield02),
    INDEX `uidIndex` (cid, uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE prg_date_rights (
    object_id INT4 UNSIGNED NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    id INT4 NOT NULL,
    type INT4 UNSIGNED NOT NULL,
    ma VARCHAR(286),
    dn VARCHAR(320),
    PRIMARY KEY (cid, object_id, id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_date_rights (
    object_id INT4 UNSIGNED NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    id INT4 NOT NULL,
    type INT4 UNSIGNED NOT NULL,
    ma VARCHAR(286),
    dn VARCHAR(320),
    PRIMARY KEY (cid, object_id, id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_dates (
    creating_date timestamp DEFAULT CURRENT_TIMESTAMP,
    created_from INT4 UNSIGNED NOT NULL,
    changing_date INT8 NOT NULL,
    changed_from INT4 UNSIGNED NOT NULL,
    fid INT4 UNSIGNED NOT NULL,
    pflag INT4 UNSIGNED NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    timestampfield01 DATETIME,
    timestampfield02 DATETIME,
    timezone VARCHAR(64),
    intfield01 INT4 UNSIGNED NOT NULL,
    intfield02 INT4 UNSIGNED,
    intfield03 INT4 UNSIGNED,
    intfield04 INT4 UNSIGNED,
    intfield05 INT4 UNSIGNED,
    intfield06 INT4 UNSIGNED,
    intfield07 INT4 UNSIGNED,
    intfield08 INT4 UNSIGNED,
    field01 VARCHAR(255),
    field02 VARCHAR(255),
    field04 TEXT,
    field06 VARCHAR(255),
    field07 text,
    field08 TEXT,
    field09 VARCHAR(255),
    uid VARCHAR(1024),
    organizer VARCHAR(255),
    sequence INT4 UNSIGNED,
    organizerId INT4 UNSIGNED,
    principal VARCHAR(255),
    principalId INT4 UNSIGNED,
    filename VARCHAR(255),
    PRIMARY KEY (cid, intfield01),
    INDEX (cid, intfield02),
    INDEX (cid, timestampfield01),
    INDEX (cid, timestampfield02),
    INDEX `uidIndex` (cid, uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_dates_members (
    object_id INT4,
    member_uid INT4,    
    confirm INT4 UNSIGNED NOT NULL,
    reason TEXT,
    pfid INT4,
    reminder INT4 UNSIGNED,
    cid INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid, object_id, member_uid),
    UNIQUE INDEX member (cid, member_uid, object_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE prg_dates_members (
    object_id INT4,
    member_uid INT4,
    confirm INT4 UNSIGNED NOT NULL,
    reason TEXT,
    pfid INT4,
    reminder INT4 UNSIGNED,
    cid INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid, object_id, member_uid),
    UNIQUE INDEX member (cid, member_uid, object_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE dateExternal (
    cid INT4 UNSIGNED NOT NULL,
	objectId INT4 UNSIGNED NOT NULL,
	mailAddress VARCHAR(255) NOT NULL,
	displayName VARCHAR(255),
	confirm INT4 UNSIGNED NOT NULL,
	reason TEXT,
	PRIMARY KEY (cid,objectId,mailAddress),
	FOREIGN KEY (cid,objectId) REFERENCES prg_dates(cid,intfield01)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE delDateExternal (
    cid INT4 UNSIGNED NOT NULL,
	objectId INT4 UNSIGNED NOT NULL,
	mailAddress VARCHAR(255) NOT NULL,
	displayName VARCHAR(255),
	confirm INT4 UNSIGNED NOT NULL,
	reason TEXT,
	PRIMARY KEY (cid,objectId,mailAddress),
	FOREIGN KEY (cid,objectId) REFERENCES del_dates(cid,intfield01)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
