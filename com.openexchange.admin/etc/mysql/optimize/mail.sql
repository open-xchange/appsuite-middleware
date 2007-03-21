#@(#) mail.sql optimizations


CREATE TABLE mail_domains (
    cid INT4 UNSIGNED,
    id INT4 UNSIGNED,
	domainName VARCHAR(128),
	smtpSenderRule VARCHAR(128),
	restriction VARCHAR(64),
    PRIMARY KEY (cid, id),
    INDEX (domainName)
);

CREATE TABLE user_mail_restrictions (
	cid INT4 UNSIGNED,
	id INT4 UNSIGNED,
	className VARCHAR(64),
	address VARCHAR(255),
	INDEX(className, address)
);

CREATE TABLE address_mappings (
	cid INT4 UNSIGNED,
	id INT4 UNSIGNED,
	address VARCHAR(255),
	destination VARCHAR(255),
    PRIMARY KEY (cid, id),
	INDEX(address)
);

# amavis

CREATE TABLE policy (
  id         INT4 UNSIGNED,
  cid        INT4 UNSIGNED,
  policy_name      varchar(32),     -- not used by amavisd-new

  virus_lover          char(1) default NULL,     -- Y/N
  spam_lover           char(1) default NULL,     -- Y/N
  banned_files_lover   char(1) default NULL,     -- Y/N
  bad_header_lover     char(1) default NULL,     -- Y/N

  bypass_virus_checks  char(1) default NULL,     -- Y/N
  bypass_spam_checks   char(1) default NULL,     -- Y/N
  bypass_banned_checks char(1) default NULL,     -- Y/N
  bypass_header_checks char(1) default NULL,     -- Y/N

  spam_modifies_subj   char(1) default NULL,     -- Y/N

  virus_quarantine_to      varchar(64) default NULL,
  spam_quarantine_to       varchar(64) default NULL,
  banned_quarantine_to     varchar(64) default NULL,
  bad_header_quarantine_to varchar(64) default NULL,

  spam_tag_level  float default NULL,  -- higher score inserts spam info headers
  spam_tag2_level float default NULL,  -- inserts 'declared spam' header fields
  spam_kill_level float default NULL,  -- higher score activates evasive actions, e.g.
                                       -- reject/drop, quarantine, ...
                                     -- (subject to final_spam_destiny setting)
  spam_dsn_cutoff_level float default NULL,

  addr_extension_virus      varchar(64) default NULL,
  addr_extension_spam       varchar(64) default NULL,
  addr_extension_banned     varchar(64) default NULL,
  addr_extension_bad_header varchar(64) default NULL,

  warnvirusrecip      char(1)     default NULL, -- Y/N
  warnbannedrecip     char(1)     default NULL, -- Y/N
  warnbadhrecip       char(1)     default NULL, -- Y/N
  newvirus_admin      varchar(64) default NULL,
  virus_admin         varchar(64) default NULL,
  banned_admin        varchar(64) default NULL,
  bad_header_admin    varchar(64) default NULL,
  spam_admin          varchar(64) default NULL,
  spam_subject_tag    varchar(64) default NULL,
  spam_subject_tag2   varchar(64) default NULL,
  message_size_limit  integer     default NULL, -- max size in bytes, 0 disable
  banned_rulenames    varchar(64) default NULL  -- comma-separated list of ...
        -- names mapped through %banned_rules to actual banned_filename tables
);

