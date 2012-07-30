CREATE COLUMNFAMILY sessionstorage ( KEY varchar PRIMARY KEY, loginName varchar, password varchar, contextId int, userId int, secret varchar, login varchar, randomToken varchar, localIp varchar, authId varchar, hash varchar, client varchar, altId varchar) WITH comparator = varchar ;
CREATE INDEX randomToken ON sessionstorage (randomToken ) ;
