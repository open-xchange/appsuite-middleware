CREATE TABLE context_sequence (
    id INT4 UNSIGNED,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO context_sequence VALUES (0);

DROP PROCEDURE IF EXISTS get_context_id;
DELIMITER //
CREATE PROCEDURE get_context_id() NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    UPDATE context_sequence SET id=id+1;
    SELECT id FROM context_sequence;
END//
DELIMITER ;
