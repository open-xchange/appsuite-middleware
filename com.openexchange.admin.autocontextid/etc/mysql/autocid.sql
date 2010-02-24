CREATE TABLE context_sequence (
    id INT4 UNSIGNED AUTO_INCREMENT,
    PRIMARY KEY (id)
);

INSERT INTO context_sequence VALUES (0);

DROP PROCEDURE IF EXISTS get_context_id;
DELIMITER //
CREATE PROCEDURE get_context_id() NOT DETERMINISTIC MODIFIES SQL DATA
BEGIN
    DECLARE identifier INT4 UNSIGNED;
    SET identifier = 0;
    SELECT id INTO identifier FROM context_sequence FOR UPDATE;
    IF 0 = identifier THEN
        INSERT INTO context_sequence (id) VALUES (identifier);
    END IF;
    SET identifier = identifier + 1;
    UPDATE context_sequence SET id = identifier;
    SELECT identifier;
END//
DELIMITER ;
