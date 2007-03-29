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
