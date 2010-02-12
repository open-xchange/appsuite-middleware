CREATE DATABASE oxreport;

GRANT ALL PRIVILEGES ON oxreport.* TO 'oxreport'@'%' IDENTIFIED BY 'secret';

CREATE TABLE report_list (
      id BIGINT NOT NULL AUTO_INCREMENT, 
      license_keys VARCHAR(767) NOT NULL,
      last_syncdate DATETIME NOT NULL, 
      current_revision BIGINT NOT NULL,
      PRIMARY KEY (id), UNIQUE (license_keys)
) ENGINE=InnoDB;

CREATE TABLE report_revisions (
      id BIGINT NOT NULL AUTO_INCREMENT,
      report_revision BIGINT NOT NULL,
      reporter_id BIGINT NOT NULL,
      connection_information LONGTEXT NOT NULL, 
      syncdate DATETIME NOT NULL,
      report LONGTEXT NOT NULL, 
      PRIMARY KEY (id), UNIQUE (id, report_revision, reporter_id)
) ENGINE=InnoDB;

CREATE TABLE report_reminder_whitelist (
      id BIGINT NOT NULL AUTO_INCREMENT,
      ldb_login VARCHAR(767) NOT NULL,
      PRIMARY KEY (id), UNIQUE (ldb_login)
) ENGINE=InnoDB;