CREATE TABLE subadmin ( /* HADM */
    sid INT4 UNSIGNED NOT NULL,
    pid INT4 UNSIGNED NOT NULL, /* parent ID */
    name VARCHAR(128) NOT NULL,
    displayName VARCHAR(128) NOT NULL,
    password VARCHAR(128) NOT NULL,
    passwordMech VARCHAR(32) NOT NULL,
    CONSTRAINT name_unique UNIQUE (name),
    PRIMARY KEY (sid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE restrictions (
    rid INT4 UNSIGNED NOT NULL,
    name VARCHAR(128) NOT NULL,
    CONSTRAINT name_unique UNIQUE (name),
    PRIMARY KEY (rid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE subadmin_restrictions (
    sid INT4 UNSIGNED NOT NULL,
    rid INT4 UNSIGNED NOT NULL,
    value VARCHAR(128) NOT NULL,
    CONSTRAINT sid_rid_unique UNIQUE (sid,rid),
    FOREIGN KEY(sid) REFERENCES subadmin(sid),
    FOREIGN KEY(rid) REFERENCES restrictions(rid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE context_restrictions (
    cid INT4 UNSIGNED NOT NULL,
    rid INT4 UNSIGNED NOT NULL,
    value VARCHAR(128) NOT NULL,
    CONSTRAINT cid_rid_unique UNIQUE (cid,rid),
    FOREIGN KEY(cid) REFERENCES context(cid),
    FOREIGN KEY(rid) REFERENCES restrictions(rid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE context2subadmin (
    cid INT4 UNSIGNED NOT NULL,
    sid INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid,sid),
    INDEX (sid),
    UNIQUE (cid),
    FOREIGN KEY(sid) REFERENCES subadmin(sid),
    FOREIGN KEY(cid) REFERENCES context(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE context_customfields (
    cid INT4 UNSIGNED NOT NULL,
    customid VARCHAR(128),
    createTimestamp INT8 NOT NULL,
    modifyTimestamp INT8 NOT NULL,
    CONSTRAINT cid_unique UNIQUE (cid),
    FOREIGN KEY(cid) REFERENCES context(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
