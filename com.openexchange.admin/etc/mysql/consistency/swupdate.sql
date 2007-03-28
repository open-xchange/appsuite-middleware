#@(#) swupdate.sql consistency

ALTER TABLE swupdate
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY monday TIME default NULL,
    MODIFY tuesday TIME default NULL,
    MODIFY wednesday TIME default NULL,
    MODIFY thursday TIME default NULL,
    MODIFY friday TIME default NULL,
    MODIFY saturday TIME default NULL,
    MODIFY sunday TIME default NULL,
    MODIFY `interval` INT2 UNSIGNED default NULL,
    ENGINE=InnoDB;
