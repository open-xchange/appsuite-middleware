CREATE TABLE sequence_context (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO sequence_context VALUES (0,0);
