#@(#) swupdate.sql optimizations

CREATE TABLE swupdate (
    cid INT4 UNSIGNED,
    monday TIME NULL,
    tuesday TIME NULL,
    wednesday TIME NULL,
    thursday TIME NULL,
    friday TIME NULL,
    saturday TIME NULL,
    sunday TIME NULL,
    `interval` INT2 UNSIGNED NULL,
    PRIMARY KEY (cid)
);
#@(#) swupdate.sql consistency

ALTER TABLE swupdate
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY monday TIME default NULL,
    MODIFY tuesday TIME default NULL,
    MODIFY wednesday TIME default NULL,
    MODIFY thursday TIME default NULL,
    MODIFY friday TIME default NULL,
    MODIFY saturday TIME default NULL,
    MODIFY sunday TIME default NULL,
    MODIFY `interval` INT2 UNSIGNED default NULL,
    ENGINE=InnoDB;
