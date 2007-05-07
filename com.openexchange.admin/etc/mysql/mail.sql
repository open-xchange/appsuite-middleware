#@(#) mail.sql optimizations

CREATE TABLE mail_domains (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
	domainName VARCHAR(128) NOT NULL,
	smtpSenderRule VARCHAR(128) NOT NULL,
	restriction VARCHAR(64) NOT NULL,
    PRIMARY KEY (cid, id),
    INDEX (domainName),
    CONSTRAINT mail_domains_domainName_unique UNIQUE (domainName)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE user_mail_restrictions (
	cid INT4 UNSIGNED NOT NULL,
	id INT4 UNSIGNED NOT NULL,
	className VARCHAR(64) NOT NULL,
	address VARCHAR(255) NOT NULL,
    PRIMARY KEY (cid, id),
	INDEX(className),
	INDEX(address),
    CONSTRAINT user_mail_restrictions_address_unique UNIQUE (address)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE group_address_mappings (
	cid INT4 UNSIGNED NOT NULL,
	id INT4 UNSIGNED NOT NULL,
	address VARCHAR(255) NOT NULL,
    PRIMARY KEY (cid, id),
	INDEX(address),
    CONSTRAINT group_address_mappings_address_unique UNIQUE (address),
    FOREIGN KEY (cid, id) REFERENCES groups(cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE resource_address_mappings (
	cid INT4 UNSIGNED NOT NULL,
	id INT4 UNSIGNED NOT NULL,
	destination VARCHAR(255) NOT NULL,
    PRIMARY KEY (cid, id),
    FOREIGN KEY (cid, id) REFERENCES resource(cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE address_mappings (
	cid INT4 UNSIGNED NOT NULL,
	id INT4 UNSIGNED NOT NULL,
	address VARCHAR(255) NOT NULL,
	destination VARCHAR(255) NOT NULL,
    PRIMARY KEY (cid, id),
	INDEX(address),
	CONSTRAINT address_mappings_address_unique UNIQUE (address)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
