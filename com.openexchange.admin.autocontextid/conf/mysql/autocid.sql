CREATE TABLE sequence_context (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sequence_context VALUES (0,0);
