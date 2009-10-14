CREATE DATABASE oxreport;

GRANT ALL PRIVILEGES ON oxreport.* TO 'oxreport'@'%' IDENTIFIED BY 'secret';

CREATE TABLE reports (
      id BIGINT NOT NULL AUTO_INCREMENT, 
      license_keys VARCHAR(767) NOT NULL, 
      connection_information LONGTEXT NOT NULL, 
      last_syncdate DATETIME NOT NULL, 
      client_information LONGTEXT NOT NULL, 
      PRIMARY KEY (id), UNIQUE (license_keys)
) ENGINE=InnoDB;