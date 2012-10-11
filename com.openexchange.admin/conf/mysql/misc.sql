#@(#) Tables for links, reminder and filestore quota.

CREATE TABLE prg_links (
    firstid INT4 UNSIGNED NOT NULL,
    firstmodule INT4 UNSIGNED NOT NULL,
    firstfolder INT4 UNSIGNED NOT NULL,
    secondid INT4 UNSIGNED NOT NULL,
    secondmodule INT4 UNSIGNED NOT NULL,
    secondfolder INT4 UNSIGNED NOT NULL,
    cid INT4 UNSIGNED NOT NULL,
    last_modified INT8,
    created_by INT4 UNSIGNED,
    INDEX (firstid),
    INDEX (secondid),
    INDEX (cid)
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE reminder (
    cid INT4 UNSIGNED NOT NULL,
    object_id INT4 UNSIGNED NOT NULL,
    last_modified INT8 UNSIGNED,
    target_id VARCHAR(255) NOT NULL,
    module INT1 UNSIGNED NOT NULL,
    userid INT4 UNSIGNED NOT NULL,
    alarm DATETIME NOT NULL,
    recurrence TINYINT NOT NULL,
    description VARCHAR(1028),
    folder VARCHAR(1028),
    PRIMARY KEY (cid,object_id),
    INDEX (cid,userid,alarm),
    INDEX (cid,userid,last_modified),
    CONSTRAINT reminder_unique UNIQUE (cid,target_id,module,userid)
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE filestore_usage (
    cid INT4 UNSIGNED NOT NULL,
    used INT8 NOT NULL,
    PRIMARY KEY(cid)
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
