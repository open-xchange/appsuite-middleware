CREATE TABLE sequence_context (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO sequence_context VALUES (0,0);

DROP PROCEDURE IF EXISTS get_context_id;
DELIMITER //
CREATE PROCEDURE get_context_id() NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    START TRANSACTION;
        UPDATE sequence_context SET id=id+1 WHERE cid=0;
        SELECT id FROM sequence_context WHERE cid=0;
    COMMIT;
END//
DELIMITER ;
