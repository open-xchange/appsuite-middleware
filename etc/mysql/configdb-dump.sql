-- MySQL dump 10.10
--
-- Host: localhost    Database: configdb
-- ------------------------------------------------------
-- Server version	5.0.22-Debian_0ubuntu6.06.2-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `configdb_sequence`
--

DROP TABLE IF EXISTS `configdb_sequence`;
CREATE TABLE `configdb_sequence` (
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `configdb_sequence`
--


/*!40000 ALTER TABLE `configdb_sequence` DISABLE KEYS */;
LOCK TABLES `configdb_sequence` WRITE;
INSERT INTO `configdb_sequence` VALUES (6);
UNLOCK TABLES;
/*!40000 ALTER TABLE `configdb_sequence` ENABLE KEYS */;

--
-- Table structure for table `context`
--

DROP TABLE IF EXISTS `context`;
CREATE TABLE `context` (
  `cid` int(10) unsigned NOT NULL,
  `name` varchar(128) NOT NULL,
  `enabled` tinyint(1) default NULL,
  `reason_id` int(10) unsigned default NULL,
  `filestore_id` int(10) unsigned default NULL,
  `filestore_name` varchar(32) default NULL,
  `filestore_login` varchar(32) default NULL,
  `filestore_passwd` varchar(32) default NULL,
  `quota_max` bigint(20) default NULL,
  PRIMARY KEY  (`cid`),
  KEY `filestore_id` (`filestore_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `context`
--


/*!40000 ALTER TABLE `context` DISABLE KEYS */;
LOCK TABLES `context` WRITE;
INSERT INTO `context` VALUES (1,'1',1,NULL,3,'1_ctx_store',NULL,NULL,-1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `context` ENABLE KEYS */;

--
-- Table structure for table `context_server2db_pool`
--

DROP TABLE IF EXISTS `context_server2db_pool`;
CREATE TABLE `context_server2db_pool` (
  `server_id` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `read_db_pool_id` int(10) unsigned NOT NULL,
  `write_db_pool_id` int(10) unsigned NOT NULL,
  `db_schema` varchar(32) NOT NULL,
  PRIMARY KEY  (`cid`,`server_id`),
  KEY `write_db_pool_id` (`write_db_pool_id`),
  KEY `server_id` (`server_id`),
  KEY `db_schema` (`db_schema`),
  CONSTRAINT `context_server2db_pool_ibfk_1` FOREIGN KEY (`cid`) REFERENCES `context` (`cid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `context_server2db_pool`
--


/*!40000 ALTER TABLE `context_server2db_pool` DISABLE KEYS */;
LOCK TABLES `context_server2db_pool` WRITE;
INSERT INTO `context_server2db_pool` VALUES (2,1,4,4,'open-xchange-db');
UNLOCK TABLES;
/*!40000 ALTER TABLE `context_server2db_pool` ENABLE KEYS */;

--
-- Table structure for table `db_cluster`
--

DROP TABLE IF EXISTS `db_cluster`;
CREATE TABLE `db_cluster` (
  `cluster_id` int(10) unsigned NOT NULL,
  `read_db_pool_id` int(10) unsigned NOT NULL,
  `write_db_pool_id` int(10) unsigned NOT NULL,
  `weight` int(10) unsigned default NULL,
  `max_units` int(11) default NULL,
  PRIMARY KEY  (`cluster_id`),
  KEY `write_db_pool_id` (`write_db_pool_id`),
  CONSTRAINT `db_cluster_ibfk_1` FOREIGN KEY (`write_db_pool_id`) REFERENCES `db_pool` (`db_pool_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `db_cluster`
--


/*!40000 ALTER TABLE `db_cluster` DISABLE KEYS */;
LOCK TABLES `db_cluster` WRITE;
INSERT INTO `db_cluster` VALUES (5,0,4,100,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `db_cluster` ENABLE KEYS */;

--
-- Table structure for table `db_pool`
--

DROP TABLE IF EXISTS `db_pool`;
CREATE TABLE `db_pool` (
  `db_pool_id` int(10) unsigned NOT NULL,
  `url` varchar(255) NOT NULL,
  `driver` varchar(128) NOT NULL,
  `login` varchar(128) NOT NULL,
  `password` varchar(128) NOT NULL,
  `hardlimit` int(11) default NULL,
  `max` int(11) default NULL,
  `initial` int(11) default NULL,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY  (`db_pool_id`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `db_pool`
--


/*!40000 ALTER TABLE `db_pool` DISABLE KEYS */;
LOCK TABLES `db_pool` WRITE;
INSERT INTO `db_pool` VALUES (4,'jdbc:mysql://localhost/?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000','com.mysql.jdbc.Driver','@OXDB_USER@','@OXDB_PASS@',0,0,0,'open-xchange-db');
UNLOCK TABLES;
/*!40000 ALTER TABLE `db_pool` ENABLE KEYS */;

--
-- Table structure for table `filestore`
--

DROP TABLE IF EXISTS `filestore`;
CREATE TABLE `filestore` (
  `id` int(10) unsigned NOT NULL,
  `uri` varchar(255) NOT NULL,
  `size` bigint(20) unsigned NOT NULL,
  `max_context` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `filestore_uri_unique` (`uri`),
  KEY `max_context` (`max_context`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `filestore`
--


/*!40000 ALTER TABLE `filestore` DISABLE KEYS */;
LOCK TABLES `filestore` WRITE;
INSERT INTO `filestore` VALUES (3,'file:@FILESPOOLPATH@/',8796093022208,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `filestore` ENABLE KEYS */;

--
-- Table structure for table `login2context`
--

DROP TABLE IF EXISTS `login2context`;
CREATE TABLE `login2context` (
  `cid` int(10) unsigned NOT NULL,
  `login_info` varchar(128) NOT NULL,
  PRIMARY KEY  (`login_info`),
  KEY `cid` (`cid`),
  CONSTRAINT `login2context_ibfk_1` FOREIGN KEY (`cid`) REFERENCES `context` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `login2context`
--


/*!40000 ALTER TABLE `login2context` DISABLE KEYS */;
LOCK TABLES `login2context` WRITE;
INSERT INTO `login2context` VALUES (1,'defaultcontext');
UNLOCK TABLES;
/*!40000 ALTER TABLE `login2context` ENABLE KEYS */;

--
-- Table structure for table `reason_text`
--

DROP TABLE IF EXISTS `reason_text`;
CREATE TABLE `reason_text` (
  `id` int(10) unsigned NOT NULL,
  `text` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `reason_text`
--


/*!40000 ALTER TABLE `reason_text` DISABLE KEYS */;
LOCK TABLES `reason_text` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `reason_text` ENABLE KEYS */;

--
-- Table structure for table `server`
--

DROP TABLE IF EXISTS `server`;
CREATE TABLE `server` (
  `server_id` int(10) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`server_id`),
  UNIQUE KEY `server_name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `server`
--


/*!40000 ALTER TABLE `server` DISABLE KEYS */;
LOCK TABLES `server` WRITE;
INSERT INTO `server` VALUES (2,'local');
UNLOCK TABLES;
/*!40000 ALTER TABLE `server` ENABLE KEYS */;

--
-- Table structure for table `server2db_pool`
--

DROP TABLE IF EXISTS `server2db_pool`;
CREATE TABLE `server2db_pool` (
  `id` int(10) unsigned NOT NULL,
  `server_id` int(10) unsigned NOT NULL,
  `db_pool_id` int(10) unsigned NOT NULL,
  `default_read` tinyint(1) NOT NULL,
  `default_write` tinyint(1) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `server2db_pool_unique` (`server_id`,`db_pool_id`),
  KEY `db_pool_id` (`db_pool_id`),
  CONSTRAINT `server2db_pool_ibfk_1` FOREIGN KEY (`db_pool_id`) REFERENCES `db_pool` (`db_pool_id`),
  CONSTRAINT `server2db_pool_ibfk_2` FOREIGN KEY (`server_id`) REFERENCES `server` (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `server2db_pool`
--


/*!40000 ALTER TABLE `server2db_pool` DISABLE KEYS */;
LOCK TABLES `server2db_pool` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `server2db_pool` ENABLE KEYS */;

--
-- Table structure for table `user_sequence`
--

DROP TABLE IF EXISTS `user_sequence`;
CREATE TABLE `user_sequence` (
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `user_sequence`
--


/*!40000 ALTER TABLE `user_sequence` DISABLE KEYS */;
LOCK TABLES `user_sequence` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_sequence` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

