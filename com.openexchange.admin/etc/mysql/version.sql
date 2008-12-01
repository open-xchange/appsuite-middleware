#@(#) Table for storing the schema version.

CREATE TABLE version (
    version INT4 UNSIGNED NOT NULL,
    locked BOOLEAN NOT NULL,
    gw_compatible BOOLEAN NOT NULL,
    admin_compatible BOOLEAN NOT NULL,
    server VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL
) ENGINE = InnoDB DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci;
