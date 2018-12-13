# Load data to ip2location table
LOAD DATA LOCAL
    INFILE '/absolute/path/to/the/Ip2Location.csv'
INTO TABLE
    `ip2location`
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 0 LINES;