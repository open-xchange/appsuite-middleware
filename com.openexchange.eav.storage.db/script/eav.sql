#@(#) Initial tables for EAV

CREATE TABLE eav_pathIndex (
    cid INT4 UNSIGNED NOT NULL,
    module INT1 UNSIGNED NOT NULL,
    objectId INT4 UNSIGNED NOT NULL,
    intTable VARCHAR(64),
    textTable VARCHAR(64),
    varcharTable VARCHAR(64),
    blobTable VARCHAR(64),
    boolTable VARCHAR(64),
    referenceTable VARCHAR(64),
    pathTable VARCHAR(64) NOT NULL,
    PRIMARY KEY (cid, module, objectId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE eav_paths1 (
    cid INT4 UNSIGNED NOT NULL,
    module INT1 UNSIGNED NOT NULL,
    objectId INT4 UNSIGNED NOT NULL,
    nodeId INT4 UNSIGNED NOT NULL,
    name VARCHAR(128),
    parent INT4 UNSIGNED NOT NULL,
    eavType VARCHAR(64) NOT NULL,
    PRIMARY KEY (cid, module, objectId),
    FOREIGN KEY (parent) REFERENCES eav_paths1(nodeId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE eav_int1 (
    cid INT4 UNSIGNED NOT NULL,
    containerType VARCHAR(64) NOT NULL,
    nodeId INT4 UNSIGNED NOT NULL,
    payload INT8
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE eav_bool1 (
    cid INT4 UNSIGNED NOT NULL,
    containerType VARCHAR(64) NOT NULL,
    nodeId INT4 UNSIGNED NOT NULL,
    payload BOOLEAN
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE eav_varchar1 (
    cid INT4 UNSIGNED NOT NULL,
    containerType VARCHAR(64) NOT NULL,
    nodeId INT4 UNSIGNED NOT NULL,
    payload VARCHAR(1024)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE eav_text1 (
    cid INT4 UNSIGNED NOT NULL,
    containerType VARCHAR(64) NOT NULL,
    nodeId INT4 UNSIGNED NOT NULL,
    payload TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE eav_blob1 (
    cid INT4 UNSIGNED NOT NULL,
    containerType VARCHAR(64) NOT NULL,
    nodeId INT4 UNSIGNED NOT NULL,
    payload BLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE eav_reference1 (
    cid INT4 UNSIGNED NOT NULL,
    containerType VARCHAR(64) NOT NULL,
    nodeId INT4 UNSIGNED NOT NULL,
    payload VARCHAR(1024)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE sequence_uid_eav_node (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;