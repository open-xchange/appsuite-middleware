#@(#) swupdate.sql

CREATE TABLE swupdate (
    cid INT4 UNSIGNED NOT NULL,
    monday TIME default NULL,
    tuesday TIME default NULL,
    wednesday TIME default NULL,
    thursday TIME default NULL,
    friday TIME default NULL,
    saturday TIME default NULL,
    sunday TIME default NULL,
    `interval` INT2 UNSIGNED default NULL,
    PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
