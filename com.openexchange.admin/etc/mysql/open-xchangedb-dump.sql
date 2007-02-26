-- MySQL dump 10.10
--
-- Host: localhost    Database: open-xchange-db
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
-- Table structure for table `address_mappings`
--

DROP TABLE IF EXISTS `address_mappings`;
CREATE TABLE `address_mappings` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `address` varchar(255) NOT NULL,
  `destination` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  UNIQUE KEY `address_2` (`address`),
  KEY `address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `address_mappings`
--


/*!40000 ALTER TABLE `address_mappings` DISABLE KEYS */;
LOCK TABLES `address_mappings` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `address_mappings` ENABLE KEYS */;

--
-- Table structure for table `del_attachment`
--

DROP TABLE IF EXISTS `del_attachment`;
CREATE TABLE `del_attachment` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `attached` int(10) unsigned NOT NULL,
  `module` int(10) unsigned NOT NULL,
  `del_date` bigint(20) NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`attached`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `del_attachment`
--


/*!40000 ALTER TABLE `del_attachment` DISABLE KEYS */;
LOCK TABLES `del_attachment` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_attachment` ENABLE KEYS */;

--
-- Table structure for table `del_contacts`
--

DROP TABLE IF EXISTS `del_contacts`;
CREATE TABLE `del_contacts` (
  `creating_date` bigint(20) NOT NULL,
  `created_from` int(11) NOT NULL,
  `changing_date` bigint(20) NOT NULL,
  `changed_from` int(11) default NULL,
  `fid` int(11) NOT NULL,
  `cid` int(11) NOT NULL,
  `userid` int(11) default NULL,
  `pflag` int(11) default NULL,
  `timestampfield01` date default NULL,
  `timestampfield02` date default NULL,
  `intfield01` int(11) NOT NULL,
  `intfield02` int(11) default NULL,
  `intfield03` int(11) default NULL,
  `intfield04` int(11) default NULL,
  `intfield05` int(11) default NULL,
  `intfield06` int(11) default NULL,
  `intfield07` int(11) default NULL,
  `intfield08` int(11) default NULL,
  `field01` varchar(320) collate utf8_unicode_ci default NULL,
  `field02` varchar(128) collate utf8_unicode_ci default NULL,
  `field03` varchar(128) collate utf8_unicode_ci default NULL,
  `field04` varchar(128) collate utf8_unicode_ci default NULL,
  `field05` varchar(64) collate utf8_unicode_ci default NULL,
  `field06` varchar(64) collate utf8_unicode_ci default NULL,
  `field07` varchar(64) collate utf8_unicode_ci default NULL,
  `field08` varchar(64) collate utf8_unicode_ci default NULL,
  `field09` varchar(64) collate utf8_unicode_ci default NULL,
  `field10` varchar(64) collate utf8_unicode_ci default NULL,
  `field11` varchar(64) collate utf8_unicode_ci default NULL,
  `field12` varchar(64) collate utf8_unicode_ci default NULL,
  `field13` varchar(64) collate utf8_unicode_ci default NULL,
  `field14` varchar(64) collate utf8_unicode_ci default NULL,
  `field15` varchar(64) collate utf8_unicode_ci default NULL,
  `field16` varchar(64) collate utf8_unicode_ci default NULL,
  `field17` varchar(1024) collate utf8_unicode_ci default NULL,
  `field18` varchar(64) collate utf8_unicode_ci default NULL,
  `field19` varchar(128) collate utf8_unicode_ci default NULL,
  `field20` varchar(128) collate utf8_unicode_ci default NULL,
  `field21` varchar(64) collate utf8_unicode_ci default NULL,
  `field22` varchar(64) collate utf8_unicode_ci default NULL,
  `field23` varchar(64) collate utf8_unicode_ci default NULL,
  `field24` varchar(64) collate utf8_unicode_ci default NULL,
  `field25` varchar(64) collate utf8_unicode_ci default NULL,
  `field26` varchar(64) collate utf8_unicode_ci default NULL,
  `field27` varchar(64) collate utf8_unicode_ci default NULL,
  `field28` varchar(64) collate utf8_unicode_ci default NULL,
  `field29` varchar(64) collate utf8_unicode_ci default NULL,
  `field30` varchar(64) collate utf8_unicode_ci default NULL,
  `field31` varchar(64) collate utf8_unicode_ci default NULL,
  `field32` varchar(64) collate utf8_unicode_ci default NULL,
  `field33` varchar(64) collate utf8_unicode_ci default NULL,
  `field34` varchar(1024) collate utf8_unicode_ci default NULL,
  `field35` varchar(64) collate utf8_unicode_ci default NULL,
  `field36` varchar(64) collate utf8_unicode_ci default NULL,
  `field37` varchar(64) collate utf8_unicode_ci default NULL,
  `field38` varchar(64) collate utf8_unicode_ci default NULL,
  `field39` varchar(64) collate utf8_unicode_ci default NULL,
  `field40` varchar(64) collate utf8_unicode_ci default NULL,
  `field41` varchar(64) collate utf8_unicode_ci default NULL,
  `field42` varchar(64) collate utf8_unicode_ci default NULL,
  `field43` varchar(64) collate utf8_unicode_ci default NULL,
  `field44` varchar(64) collate utf8_unicode_ci default NULL,
  `field45` varchar(64) collate utf8_unicode_ci default NULL,
  `field46` varchar(64) collate utf8_unicode_ci default NULL,
  `field47` varchar(64) collate utf8_unicode_ci default NULL,
  `field48` varchar(64) collate utf8_unicode_ci default NULL,
  `field49` varchar(64) collate utf8_unicode_ci default NULL,
  `field50` varchar(64) collate utf8_unicode_ci default NULL,
  `field51` varchar(64) collate utf8_unicode_ci default NULL,
  `field52` varchar(64) collate utf8_unicode_ci default NULL,
  `field53` varchar(64) collate utf8_unicode_ci default NULL,
  `field54` varchar(64) collate utf8_unicode_ci default NULL,
  `field55` varchar(64) collate utf8_unicode_ci default NULL,
  `field56` varchar(64) collate utf8_unicode_ci default NULL,
  `field57` varchar(64) collate utf8_unicode_ci default NULL,
  `field58` varchar(64) collate utf8_unicode_ci default NULL,
  `field59` varchar(64) collate utf8_unicode_ci default NULL,
  `field60` varchar(64) collate utf8_unicode_ci default NULL,
  `field61` varchar(64) collate utf8_unicode_ci default NULL,
  `field62` varchar(64) collate utf8_unicode_ci default NULL,
  `field63` varchar(64) collate utf8_unicode_ci default NULL,
  `field64` varchar(64) collate utf8_unicode_ci default NULL,
  `field65` varchar(256) collate utf8_unicode_ci default NULL,
  `field66` varchar(256) collate utf8_unicode_ci default NULL,
  `field67` varchar(256) collate utf8_unicode_ci default NULL,
  `field68` varchar(64) collate utf8_unicode_ci default NULL,
  `field69` varchar(64) collate utf8_unicode_ci default NULL,
  `field70` varchar(64) collate utf8_unicode_ci default NULL,
  `field71` varchar(64) collate utf8_unicode_ci default NULL,
  `field72` varchar(64) collate utf8_unicode_ci default NULL,
  `field73` varchar(64) collate utf8_unicode_ci default NULL,
  `field74` varchar(64) collate utf8_unicode_ci default NULL,
  `field75` varchar(64) collate utf8_unicode_ci default NULL,
  `field76` varchar(64) collate utf8_unicode_ci default NULL,
  `field77` varchar(64) collate utf8_unicode_ci default NULL,
  `field78` varchar(64) collate utf8_unicode_ci default NULL,
  `field79` varchar(64) collate utf8_unicode_ci default NULL,
  `field80` varchar(64) collate utf8_unicode_ci default NULL,
  `field81` varchar(64) collate utf8_unicode_ci default NULL,
  `field82` varchar(64) collate utf8_unicode_ci default NULL,
  `field83` varchar(64) collate utf8_unicode_ci default NULL,
  `field84` varchar(64) collate utf8_unicode_ci default NULL,
  `field85` varchar(64) collate utf8_unicode_ci default NULL,
  `field86` varchar(64) collate utf8_unicode_ci default NULL,
  `field87` varchar(64) collate utf8_unicode_ci default NULL,
  `field88` varchar(64) collate utf8_unicode_ci default NULL,
  `field89` varchar(64) collate utf8_unicode_ci default NULL,
  `field90` varchar(64) collate utf8_unicode_ci default NULL,
  `field91` varchar(64) collate utf8_unicode_ci default NULL,
  `field92` varchar(64) collate utf8_unicode_ci default NULL,
  `field93` varchar(64) collate utf8_unicode_ci default NULL,
  `field94` varchar(64) collate utf8_unicode_ci default NULL,
  `field95` varchar(64) collate utf8_unicode_ci default NULL,
  `field96` varchar(64) collate utf8_unicode_ci default NULL,
  `field97` varchar(64) collate utf8_unicode_ci default NULL,
  `field98` varchar(64) collate utf8_unicode_ci default NULL,
  `field99` varchar(64) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`intfield01`),
  KEY `created_from` (`created_from`),
  KEY `changing_date` (`changing_date`),
  KEY `userid` (`userid`),
  KEY `cid` (`cid`,`fid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_contacts`
--


/*!40000 ALTER TABLE `del_contacts` DISABLE KEYS */;
LOCK TABLES `del_contacts` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_contacts` ENABLE KEYS */;

--
-- Table structure for table `del_contacts_image`
--

DROP TABLE IF EXISTS `del_contacts_image`;
CREATE TABLE `del_contacts_image` (
  `intfield01` int(11) NOT NULL,
  `image1` mediumblob,
  `changing_date` bigint(20) NOT NULL,
  `mime_type` varchar(32) NOT NULL,
  `cid` int(11) NOT NULL,
  PRIMARY KEY  (`cid`,`intfield01`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `del_contacts_image`
--


/*!40000 ALTER TABLE `del_contacts_image` DISABLE KEYS */;
LOCK TABLES `del_contacts_image` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_contacts_image` ENABLE KEYS */;

--
-- Table structure for table `del_date_rights`
--

DROP TABLE IF EXISTS `del_date_rights`;
CREATE TABLE `del_date_rights` (
  `object_id` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `type` int(10) unsigned NOT NULL,
  `ma` varchar(64) default NULL,
  `dn` varchar(64) default NULL,
  PRIMARY KEY  (`cid`,`object_id`,`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `del_date_rights`
--


/*!40000 ALTER TABLE `del_date_rights` DISABLE KEYS */;
LOCK TABLES `del_date_rights` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_date_rights` ENABLE KEYS */;

--
-- Table structure for table `del_dates`
--

DROP TABLE IF EXISTS `del_dates`;
CREATE TABLE `del_dates` (
  `creating_date` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `created_from` int(10) unsigned NOT NULL,
  `changing_date` bigint(20) NOT NULL,
  `changed_from` int(10) unsigned NOT NULL,
  `fid` int(10) unsigned NOT NULL,
  `pflag` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `timestampfield01` datetime default NULL,
  `timestampfield02` datetime default NULL,
  `timezone` varchar(64) collate utf8_unicode_ci default NULL,
  `intfield01` int(10) unsigned NOT NULL,
  `intfield02` int(10) unsigned default NULL,
  `intfield03` int(10) unsigned default NULL,
  `intfield04` int(10) unsigned default NULL,
  `intfield05` int(10) unsigned default NULL,
  `intfield06` int(10) unsigned default NULL,
  `intfield07` int(10) unsigned default NULL,
  `intfield08` int(10) unsigned default NULL,
  `field01` varchar(255) collate utf8_unicode_ci default NULL,
  `field02` varchar(255) collate utf8_unicode_ci default NULL,
  `field04` text collate utf8_unicode_ci,
  `field06` varchar(255) collate utf8_unicode_ci default NULL,
  `field07` varchar(255) collate utf8_unicode_ci default NULL,
  `field08` varchar(255) collate utf8_unicode_ci default NULL,
  `field09` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`intfield01`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_dates`
--


/*!40000 ALTER TABLE `del_dates` DISABLE KEYS */;
LOCK TABLES `del_dates` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_dates` ENABLE KEYS */;

--
-- Table structure for table `del_dates_members`
--

DROP TABLE IF EXISTS `del_dates_members`;
CREATE TABLE `del_dates_members` (
  `object_id` int(11) NOT NULL default '0',
  `member_uid` int(11) NOT NULL default '0',
  `confirm` int(10) unsigned NOT NULL,
  `reason` varchar(255) collate utf8_unicode_ci default NULL,
  `pfid` int(11) default NULL,
  `reminder` int(10) unsigned default NULL,
  `cid` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`object_id`,`member_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_dates_members`
--


/*!40000 ALTER TABLE `del_dates_members` DISABLE KEYS */;
LOCK TABLES `del_dates_members` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_dates_members` ENABLE KEYS */;

--
-- Table structure for table `del_dlist`
--

DROP TABLE IF EXISTS `del_dlist`;
CREATE TABLE `del_dlist` (
  `intfield01` int(11) NOT NULL,
  `intfield02` int(11) default NULL,
  `intfield03` int(11) default NULL,
  `intfield04` int(11) default NULL,
  `field01` varchar(320) collate utf8_unicode_ci default NULL,
  `field02` varchar(128) collate utf8_unicode_ci default NULL,
  `field03` varchar(128) collate utf8_unicode_ci default NULL,
  `field04` varchar(128) collate utf8_unicode_ci default NULL,
  `cid` int(11) NOT NULL,
  KEY `intfield01` (`intfield01`,`cid`),
  KEY `intfield01_2` (`intfield01`,`intfield02`,`intfield03`,`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_dlist`
--


/*!40000 ALTER TABLE `del_dlist` DISABLE KEYS */;
LOCK TABLES `del_dlist` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_dlist` ENABLE KEYS */;

--
-- Table structure for table `del_groups`
--

DROP TABLE IF EXISTS `del_groups`;
CREATE TABLE `del_groups` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `identifier` varchar(128) collate utf8_unicode_ci NOT NULL,
  `displayName` varchar(128) collate utf8_unicode_ci NOT NULL,
  `lastModified` bigint(20) NOT NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_groups`
--


/*!40000 ALTER TABLE `del_groups` DISABLE KEYS */;
LOCK TABLES `del_groups` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_groups` ENABLE KEYS */;

--
-- Table structure for table `del_infostore`
--

DROP TABLE IF EXISTS `del_infostore`;
CREATE TABLE `del_infostore` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `folder_id` int(10) unsigned NOT NULL,
  `version` int(10) unsigned NOT NULL,
  `color_label` int(10) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `last_modified` bigint(64) NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `changed_by` int(10) unsigned default NULL,
  PRIMARY KEY  (`cid`,`id`,`folder_id`),
  KEY `last_modified` (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_infostore`
--


/*!40000 ALTER TABLE `del_infostore` DISABLE KEYS */;
LOCK TABLES `del_infostore` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_infostore` ENABLE KEYS */;

--
-- Table structure for table `del_infostore_document`
--

DROP TABLE IF EXISTS `del_infostore_document`;
CREATE TABLE `del_infostore_document` (
  `cid` int(10) unsigned NOT NULL,
  `infostore_id` int(10) unsigned NOT NULL,
  `version_number` int(10) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `last_modified` bigint(64) NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `changed_by` int(10) unsigned default NULL,
  `title` varchar(128) collate utf8_unicode_ci default NULL,
  `url` varchar(128) collate utf8_unicode_ci default NULL,
  `description` text character set utf8,
  `categories` varchar(255) collate utf8_unicode_ci default NULL,
  `filename` varchar(255) collate utf8_unicode_ci default NULL,
  `file_store_location` varchar(255) collate utf8_unicode_ci default NULL,
  `file_size` int(10) unsigned default NULL,
  `file_mimetype` varchar(255) collate utf8_unicode_ci default NULL,
  `file_md5sum` varchar(32) collate utf8_unicode_ci default NULL,
  `file_version_comment` text character set utf8,
  PRIMARY KEY  (`cid`,`infostore_id`,`version_number`),
  KEY `last_modified` (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_infostore_document`
--


/*!40000 ALTER TABLE `del_infostore_document` DISABLE KEYS */;
LOCK TABLES `del_infostore_document` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_infostore_document` ENABLE KEYS */;

--
-- Table structure for table `del_oxfolder_permissions`
--

DROP TABLE IF EXISTS `del_oxfolder_permissions`;
CREATE TABLE `del_oxfolder_permissions` (
  `cid` int(10) unsigned NOT NULL,
  `fuid` int(10) unsigned NOT NULL,
  `permission_id` int(10) unsigned NOT NULL,
  `fp` tinyint(3) unsigned NOT NULL,
  `orp` tinyint(3) unsigned NOT NULL,
  `owp` tinyint(3) unsigned NOT NULL,
  `odp` tinyint(3) unsigned NOT NULL,
  `admin_flag` tinyint(3) unsigned NOT NULL,
  `group_flag` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`permission_id`,`fuid`),
  KEY `cid` (`cid`,`fuid`),
  CONSTRAINT `del_oxfolder_permissions_ibfk_1` FOREIGN KEY (`cid`, `fuid`) REFERENCES `del_oxfolder_tree` (`cid`, `fuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `del_oxfolder_permissions`
--


/*!40000 ALTER TABLE `del_oxfolder_permissions` DISABLE KEYS */;
LOCK TABLES `del_oxfolder_permissions` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_oxfolder_permissions` ENABLE KEYS */;

--
-- Table structure for table `del_oxfolder_tree`
--

DROP TABLE IF EXISTS `del_oxfolder_tree`;
CREATE TABLE `del_oxfolder_tree` (
  `fuid` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `parent` int(10) unsigned NOT NULL,
  `fname` varchar(64) character set utf8 collate utf8_unicode_ci NOT NULL,
  `module` tinyint(3) unsigned NOT NULL,
  `type` tinyint(3) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `created_from` int(10) unsigned NOT NULL,
  `changing_date` bigint(64) NOT NULL,
  `changed_from` int(10) unsigned NOT NULL,
  `permission_flag` tinyint(3) unsigned NOT NULL,
  `subfolder_flag` tinyint(3) unsigned NOT NULL,
  `default_flag` tinyint(3) unsigned default '0',
  PRIMARY KEY  (`cid`,`fuid`),
  KEY `cid` (`cid`,`parent`),
  KEY `cid_2` (`cid`,`created_from`),
  KEY `cid_3` (`cid`,`changed_from`),
  CONSTRAINT `del_oxfolder_tree_ibfk_1` FOREIGN KEY (`cid`, `created_from`) REFERENCES `user` (`cid`, `id`),
  CONSTRAINT `del_oxfolder_tree_ibfk_2` FOREIGN KEY (`cid`, `changed_from`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `del_oxfolder_tree`
--


/*!40000 ALTER TABLE `del_oxfolder_tree` DISABLE KEYS */;
LOCK TABLES `del_oxfolder_tree` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_oxfolder_tree` ENABLE KEYS */;

--
-- Table structure for table `del_resource`
--

DROP TABLE IF EXISTS `del_resource`;
CREATE TABLE `del_resource` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `identifier` varchar(128) collate utf8_unicode_ci NOT NULL,
  `displayName` varchar(128) collate utf8_unicode_ci NOT NULL,
  `mail` varchar(256) collate utf8_unicode_ci default NULL,
  `available` tinyint(1) NOT NULL,
  `description` varchar(255) collate utf8_unicode_ci default NULL,
  `lastModified` bigint(20) NOT NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_resource`
--


/*!40000 ALTER TABLE `del_resource` DISABLE KEYS */;
LOCK TABLES `del_resource` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_resource` ENABLE KEYS */;

--
-- Table structure for table `del_resource_group`
--

DROP TABLE IF EXISTS `del_resource_group`;
CREATE TABLE `del_resource_group` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `identifier` varchar(128) collate utf8_unicode_ci NOT NULL,
  `displayName` varchar(128) collate utf8_unicode_ci NOT NULL,
  `available` tinyint(1) NOT NULL,
  `description` varchar(255) collate utf8_unicode_ci default NULL,
  `lastModified` bigint(20) NOT NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_resource_group`
--


/*!40000 ALTER TABLE `del_resource_group` DISABLE KEYS */;
LOCK TABLES `del_resource_group` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_resource_group` ENABLE KEYS */;

--
-- Table structure for table `del_task`
--

DROP TABLE IF EXISTS `del_task`;
CREATE TABLE `del_task` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `private` tinyint(1) NOT NULL,
  `creating_date` datetime NOT NULL,
  `last_modified` bigint(20) NOT NULL,
  `created_from` int(10) unsigned NOT NULL,
  `changed_from` int(10) unsigned default NULL,
  `start` datetime default NULL,
  `end` datetime default NULL,
  `completed` datetime default NULL,
  `title` varchar(128) collate utf8_unicode_ci default NULL,
  `description` text collate utf8_unicode_ci,
  `state` int(11) default NULL,
  `priority` int(11) default NULL,
  `progress` int(11) default NULL,
  `categories` varchar(255) collate utf8_unicode_ci default NULL,
  `project` int(10) unsigned default NULL,
  `target_duration` bigint(20) default NULL,
  `actual_duration` bigint(20) default NULL,
  `target_costs` float default NULL,
  `actual_costs` float default NULL,
  `currency` varchar(10) collate utf8_unicode_ci default NULL,
  `trip_meter` varchar(255) collate utf8_unicode_ci default NULL,
  `billing` varchar(255) collate utf8_unicode_ci default NULL,
  `companies` varchar(255) collate utf8_unicode_ci default NULL,
  `color_label` tinyint(3) unsigned default NULL,
  `recurrence_type` tinyint(3) unsigned NOT NULL,
  `recurrence_interval` int(10) unsigned default NULL,
  `recurrence_days` tinyint(3) unsigned default NULL,
  `recurrence_dayinmonth` tinyint(3) unsigned default NULL,
  `recurrence_month` tinyint(3) unsigned default NULL,
  `recurrence_until` datetime default NULL,
  `recurrence_count` smallint(5) unsigned default NULL,
  `number_of_attachments` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_task`
--


/*!40000 ALTER TABLE `del_task` DISABLE KEYS */;
LOCK TABLES `del_task` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_task` ENABLE KEYS */;

--
-- Table structure for table `del_task_eparticipant`
--

DROP TABLE IF EXISTS `del_task_eparticipant`;
CREATE TABLE `del_task_eparticipant` (
  `cid` int(10) unsigned NOT NULL,
  `task` int(10) unsigned NOT NULL,
  `mail` varchar(255) collate utf8_unicode_ci NOT NULL,
  `display_name` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`task`,`mail`),
  CONSTRAINT `del_task_eparticipant_ibfk_1` FOREIGN KEY (`cid`, `task`) REFERENCES `del_task` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_task_eparticipant`
--


/*!40000 ALTER TABLE `del_task_eparticipant` DISABLE KEYS */;
LOCK TABLES `del_task_eparticipant` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_task_eparticipant` ENABLE KEYS */;

--
-- Table structure for table `del_task_folder`
--

DROP TABLE IF EXISTS `del_task_folder`;
CREATE TABLE `del_task_folder` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `folder` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`id`,`folder`),
  KEY `cid` (`cid`,`folder`),
  CONSTRAINT `del_task_folder_ibfk_1` FOREIGN KEY (`cid`, `id`) REFERENCES `del_task` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_task_folder`
--


/*!40000 ALTER TABLE `del_task_folder` DISABLE KEYS */;
LOCK TABLES `del_task_folder` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_task_folder` ENABLE KEYS */;

--
-- Table structure for table `del_task_participant`
--

DROP TABLE IF EXISTS `del_task_participant`;
CREATE TABLE `del_task_participant` (
  `cid` int(10) unsigned NOT NULL,
  `task` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `group_id` int(10) unsigned default NULL,
  `accepted` tinyint(3) unsigned NOT NULL,
  `description` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`task`,`user`),
  CONSTRAINT `del_task_participant_ibfk_1` FOREIGN KEY (`cid`, `task`) REFERENCES `del_task` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_task_participant`
--


/*!40000 ALTER TABLE `del_task_participant` DISABLE KEYS */;
LOCK TABLES `del_task_participant` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_task_participant` ENABLE KEYS */;

--
-- Table structure for table `del_user`
--

DROP TABLE IF EXISTS `del_user`;
CREATE TABLE `del_user` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `imapServer` varchar(128) collate utf8_unicode_ci default NULL,
  `imapLogin` varchar(128) collate utf8_unicode_ci default NULL,
  `mail` varchar(256) collate utf8_unicode_ci NOT NULL,
  `mailDomain` varchar(128) collate utf8_unicode_ci default NULL,
  `mailEnabled` tinyint(1) NOT NULL,
  `preferredLanguage` varchar(10) collate utf8_unicode_ci NOT NULL,
  `shadowLastChange` int(11) NOT NULL,
  `smtpServer` varchar(128) collate utf8_unicode_ci default NULL,
  `timeZone` varchar(128) collate utf8_unicode_ci NOT NULL,
  `contactId` int(10) unsigned NOT NULL,
  `lastModified` bigint(20) NOT NULL,
  `userPassword` varchar(128) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `del_user`
--


/*!40000 ALTER TABLE `del_user` DISABLE KEYS */;
LOCK TABLES `del_user` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `del_user` ENABLE KEYS */;

--
-- Table structure for table `filestore_usage`
--

DROP TABLE IF EXISTS `filestore_usage`;
CREATE TABLE `filestore_usage` (
  `cid` int(10) unsigned NOT NULL,
  `used` bigint(20) NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `filestore_usage`
--


/*!40000 ALTER TABLE `filestore_usage` DISABLE KEYS */;
LOCK TABLES `filestore_usage` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `filestore_usage` ENABLE KEYS */;

--
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS `groups`;
CREATE TABLE `groups` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `identifier` varchar(128) collate utf8_unicode_ci NOT NULL,
  `displayName` varchar(128) collate utf8_unicode_ci NOT NULL,
  `lastModified` bigint(20) NOT NULL,
  `gidNumber` int(10) unsigned NOT NULL default '5000',
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `groups`
--


/*!40000 ALTER TABLE `groups` DISABLE KEYS */;
LOCK TABLES `groups` WRITE;
INSERT INTO `groups` VALUES (1,1,'users','All Users',1171290473460,5000);
UNLOCK TABLES;
/*!40000 ALTER TABLE `groups` ENABLE KEYS */;

--
-- Table structure for table `groups_member`
--

DROP TABLE IF EXISTS `groups_member`;
CREATE TABLE `groups_member` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `member` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`id`,`member`),
  KEY `cid` (`cid`,`member`),
  CONSTRAINT `groups_member_ibfk_1` FOREIGN KEY (`cid`, `id`) REFERENCES `groups` (`cid`, `id`),
  CONSTRAINT `groups_member_ibfk_2` FOREIGN KEY (`cid`, `member`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `groups_member`
--


/*!40000 ALTER TABLE `groups_member` DISABLE KEYS */;
LOCK TABLES `groups_member` WRITE;
INSERT INTO `groups_member` VALUES (1,1,2);
UNLOCK TABLES;
/*!40000 ALTER TABLE `groups_member` ENABLE KEYS */;

--
-- Table structure for table `ical_ids`
--

DROP TABLE IF EXISTS `ical_ids`;
CREATE TABLE `ical_ids` (
  `object_id` int(11) NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `principal_id` int(11) NOT NULL,
  `client_id` text,
  `target_object_id` int(11) default NULL,
  `module` int(11) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `ical_ids`
--


/*!40000 ALTER TABLE `ical_ids` DISABLE KEYS */;
LOCK TABLES `ical_ids` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `ical_ids` ENABLE KEYS */;

--
-- Table structure for table `ical_principal`
--

DROP TABLE IF EXISTS `ical_principal`;
CREATE TABLE `ical_principal` (
  `object_id` int(11) NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `principal` text NOT NULL,
  `calendarfolder` int(11) NOT NULL,
  `taskfolder` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `ical_principal`
--


/*!40000 ALTER TABLE `ical_principal` DISABLE KEYS */;
LOCK TABLES `ical_principal` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `ical_principal` ENABLE KEYS */;

--
-- Table structure for table `infostore`
--

DROP TABLE IF EXISTS `infostore`;
CREATE TABLE `infostore` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `folder_id` int(10) unsigned NOT NULL,
  `version` int(10) unsigned NOT NULL,
  `locked_until` bigint(64) unsigned default NULL,
  `color_label` int(10) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `last_modified` bigint(64) NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `changed_by` int(10) unsigned default NULL,
  PRIMARY KEY  (`cid`,`id`,`folder_id`),
  KEY `last_modified` (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `infostore`
--


/*!40000 ALTER TABLE `infostore` DISABLE KEYS */;
LOCK TABLES `infostore` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `infostore` ENABLE KEYS */;

--
-- Table structure for table `infostore_document`
--

DROP TABLE IF EXISTS `infostore_document`;
CREATE TABLE `infostore_document` (
  `cid` int(10) unsigned NOT NULL,
  `infostore_id` int(10) unsigned NOT NULL,
  `version_number` int(10) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `last_modified` bigint(64) NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `changed_by` int(10) unsigned default NULL,
  `title` varchar(128) collate utf8_unicode_ci default NULL,
  `url` varchar(128) collate utf8_unicode_ci default NULL,
  `description` text character set utf8,
  `categories` varchar(255) collate utf8_unicode_ci default NULL,
  `filename` varchar(255) collate utf8_unicode_ci default NULL,
  `file_store_location` varchar(255) collate utf8_unicode_ci default NULL,
  `file_size` int(10) unsigned default NULL,
  `file_mimetype` varchar(255) collate utf8_unicode_ci default NULL,
  `file_md5sum` varchar(32) collate utf8_unicode_ci default NULL,
  `file_version_comment` text character set utf8,
  PRIMARY KEY  (`cid`,`infostore_id`,`version_number`),
  KEY `last_modified` (`last_modified`),
  CONSTRAINT `infostore_document_ibfk_1` FOREIGN KEY (`cid`, `infostore_id`) REFERENCES `infostore` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `infostore_document`
--


/*!40000 ALTER TABLE `infostore_document` DISABLE KEYS */;
LOCK TABLES `infostore_document` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `infostore_document` ENABLE KEYS */;

--
-- Table structure for table `infostore_lock`
--

DROP TABLE IF EXISTS `infostore_lock`;
CREATE TABLE `infostore_lock` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `userid` int(10) unsigned NOT NULL,
  `entity` int(10) unsigned default NULL,
  `timeout` bigint(64) unsigned NOT NULL,
  `type` tinyint(3) unsigned NOT NULL,
  `scope` tinyint(3) unsigned NOT NULL,
  `ownerDesc` varchar(128) default NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `infostore_lock`
--


/*!40000 ALTER TABLE `infostore_lock` DISABLE KEYS */;
LOCK TABLES `infostore_lock` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `infostore_lock` ENABLE KEYS */;

--
-- Table structure for table `infostore_property`
--

DROP TABLE IF EXISTS `infostore_property`;
CREATE TABLE `infostore_property` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `name` varchar(128) collate utf8_unicode_ci NOT NULL,
  `namespace` varchar(128) collate utf8_unicode_ci NOT NULL,
  `value` varchar(255) collate utf8_unicode_ci default NULL,
  `language` varchar(128) collate utf8_unicode_ci default NULL,
  `xml` tinyint(1) default NULL,
  PRIMARY KEY  (`cid`,`id`,`name`,`namespace`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `infostore_property`
--


/*!40000 ALTER TABLE `infostore_property` DISABLE KEYS */;
LOCK TABLES `infostore_property` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `infostore_property` ENABLE KEYS */;

--
-- Table structure for table `lock_null`
--

DROP TABLE IF EXISTS `lock_null`;
CREATE TABLE `lock_null` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `lock_null`
--


/*!40000 ALTER TABLE `lock_null` DISABLE KEYS */;
LOCK TABLES `lock_null` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `lock_null` ENABLE KEYS */;

--
-- Table structure for table `lock_null_lock`
--

DROP TABLE IF EXISTS `lock_null_lock`;
CREATE TABLE `lock_null_lock` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `userid` int(10) unsigned default NULL,
  `entity` int(10) unsigned default NULL,
  `timeout` bigint(64) unsigned default NULL,
  `type` tinyint(3) unsigned default NULL,
  `scope` tinyint(3) unsigned default NULL,
  `ownerDesc` varchar(128) default NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `lock_null_lock`
--


/*!40000 ALTER TABLE `lock_null_lock` DISABLE KEYS */;
LOCK TABLES `lock_null_lock` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `lock_null_lock` ENABLE KEYS */;

--
-- Table structure for table `login2user`
--

DROP TABLE IF EXISTS `login2user`;
CREATE TABLE `login2user` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `uid` varchar(128) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`cid`,`uid`),
  KEY `cid` (`cid`,`id`),
  CONSTRAINT `login2user_ibfk_1` FOREIGN KEY (`cid`, `id`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `login2user`
--


/*!40000 ALTER TABLE `login2user` DISABLE KEYS */;
LOCK TABLES `login2user` WRITE;
INSERT INTO `login2user` VALUES (1,2,'oxadmin');
UNLOCK TABLES;
/*!40000 ALTER TABLE `login2user` ENABLE KEYS */;

--
-- Table structure for table `mail_domains`
--

DROP TABLE IF EXISTS `mail_domains`;
CREATE TABLE `mail_domains` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `domainName` varchar(128) NOT NULL,
  `smtpSenderRule` varchar(128) NOT NULL,
  `restriction` varchar(64) NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  UNIQUE KEY `domainName_2` (`domainName`),
  KEY `domainName` (`domainName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mail_domains`
--


/*!40000 ALTER TABLE `mail_domains` DISABLE KEYS */;
LOCK TABLES `mail_domains` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `mail_domains` ENABLE KEYS */;

--
-- Table structure for table `mail_restrictions`
--

DROP TABLE IF EXISTS `mail_restrictions`;
CREATE TABLE `mail_restrictions` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `className` varchar(64) NOT NULL,
  `address` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  UNIQUE KEY `address` (`address`),
  KEY `className` (`className`,`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mail_restrictions`
--


/*!40000 ALTER TABLE `mail_restrictions` DISABLE KEYS */;
LOCK TABLES `mail_restrictions` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `mail_restrictions` ENABLE KEYS */;

--
-- Table structure for table `oxfolder_lock`
--

DROP TABLE IF EXISTS `oxfolder_lock`;
CREATE TABLE `oxfolder_lock` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `userid` int(10) unsigned NOT NULL,
  `entity` int(10) unsigned default NULL,
  `timeout` bigint(64) unsigned NOT NULL,
  `depth` tinyint(4) default NULL,
  `type` tinyint(3) unsigned NOT NULL,
  `scope` tinyint(3) unsigned NOT NULL,
  `ownerDesc` varchar(128) default NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `oxfolder_lock`
--


/*!40000 ALTER TABLE `oxfolder_lock` DISABLE KEYS */;
LOCK TABLES `oxfolder_lock` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `oxfolder_lock` ENABLE KEYS */;

--
-- Table structure for table `oxfolder_permissions`
--

DROP TABLE IF EXISTS `oxfolder_permissions`;
CREATE TABLE `oxfolder_permissions` (
  `cid` int(10) unsigned NOT NULL,
  `fuid` int(10) unsigned NOT NULL,
  `permission_id` int(10) unsigned NOT NULL,
  `fp` tinyint(3) unsigned NOT NULL,
  `orp` tinyint(3) unsigned NOT NULL,
  `owp` tinyint(3) unsigned NOT NULL,
  `odp` tinyint(3) unsigned NOT NULL,
  `admin_flag` tinyint(3) unsigned NOT NULL,
  `group_flag` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`permission_id`,`fuid`),
  KEY `cid` (`cid`,`fuid`),
  CONSTRAINT `oxfolder_permissions_ibfk_1` FOREIGN KEY (`cid`, `fuid`) REFERENCES `oxfolder_tree` (`cid`, `fuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `oxfolder_permissions`
--


/*!40000 ALTER TABLE `oxfolder_permissions` DISABLE KEYS */;
LOCK TABLES `oxfolder_permissions` WRITE;
INSERT INTO `oxfolder_permissions` VALUES (1,1,0,8,0,0,0,0,1),(1,2,0,8,0,0,0,0,1),(1,3,0,2,0,0,0,0,1),(1,4,0,2,0,0,0,0,1),(1,5,0,8,128,128,128,0,1),(1,6,0,2,4,0,0,0,1),(1,7,0,2,0,0,0,0,1),(1,8,0,8,4,2,2,0,1),(1,9,0,8,0,0,0,0,1),(1,5,2,128,128,128,128,1,0),(1,8,2,128,128,128,128,1,0),(1,21,2,128,128,128,128,1,0),(1,22,2,128,128,128,128,1,0),(1,23,2,128,128,128,128,1,0),(1,24,2,128,128,128,128,1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `oxfolder_permissions` ENABLE KEYS */;

--
-- Table structure for table `oxfolder_property`
--

DROP TABLE IF EXISTS `oxfolder_property`;
CREATE TABLE `oxfolder_property` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `name` varchar(128) collate utf8_unicode_ci NOT NULL,
  `namespace` varchar(128) collate utf8_unicode_ci NOT NULL,
  `value` varchar(255) collate utf8_unicode_ci default NULL,
  `language` varchar(128) collate utf8_unicode_ci default NULL,
  `xml` tinyint(1) default NULL,
  PRIMARY KEY  (`cid`,`id`,`name`,`namespace`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `oxfolder_property`
--


/*!40000 ALTER TABLE `oxfolder_property` DISABLE KEYS */;
LOCK TABLES `oxfolder_property` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `oxfolder_property` ENABLE KEYS */;

--
-- Table structure for table `oxfolder_specialfolders`
--

DROP TABLE IF EXISTS `oxfolder_specialfolders`;
CREATE TABLE `oxfolder_specialfolders` (
  `tag` varchar(16) character set utf8 collate utf8_unicode_ci NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `fuid` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`fuid`,`tag`),
  CONSTRAINT `oxfolder_specialfolders_ibfk_1` FOREIGN KEY (`cid`, `fuid`) REFERENCES `oxfolder_tree` (`cid`, `fuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `oxfolder_specialfolders`
--


/*!40000 ALTER TABLE `oxfolder_specialfolders` DISABLE KEYS */;
LOCK TABLES `oxfolder_specialfolders` WRITE;
INSERT INTO `oxfolder_specialfolders` VALUES ('private',1,1),('public',1,2),('shared',1,3),('system',1,4),('system_global',1,5),('system_ldap',1,6),('user',1,7),('projects',1,8),('infostore',1,9);
UNLOCK TABLES;
/*!40000 ALTER TABLE `oxfolder_specialfolders` ENABLE KEYS */;

--
-- Table structure for table `oxfolder_tree`
--

DROP TABLE IF EXISTS `oxfolder_tree`;
CREATE TABLE `oxfolder_tree` (
  `fuid` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `parent` int(10) unsigned NOT NULL,
  `fname` varchar(128) character set utf8 collate utf8_unicode_ci NOT NULL,
  `module` tinyint(3) unsigned NOT NULL,
  `type` tinyint(3) unsigned NOT NULL,
  `creating_date` bigint(64) NOT NULL,
  `created_from` int(10) unsigned NOT NULL,
  `changing_date` bigint(64) NOT NULL,
  `changed_from` int(10) unsigned NOT NULL,
  `permission_flag` tinyint(3) unsigned NOT NULL,
  `subfolder_flag` tinyint(3) unsigned NOT NULL,
  `default_flag` tinyint(3) unsigned default '0',
  PRIMARY KEY  (`cid`,`fuid`),
  KEY `cid` (`cid`,`parent`),
  KEY `cid_2` (`cid`,`created_from`),
  KEY `cid_3` (`cid`,`changed_from`),
  CONSTRAINT `oxfolder_tree_ibfk_1` FOREIGN KEY (`cid`, `created_from`) REFERENCES `user` (`cid`, `id`),
  CONSTRAINT `oxfolder_tree_ibfk_2` FOREIGN KEY (`cid`, `changed_from`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `oxfolder_tree`
--


/*!40000 ALTER TABLE `oxfolder_tree` DISABLE KEYS */;
LOCK TABLES `oxfolder_tree` WRITE;
INSERT INTO `oxfolder_tree` VALUES (1,1,0,'private',5,5,1171290473621,2,1171290473675,2,2,1,0),(2,1,0,'public',5,5,1171290473621,2,1171290473621,2,2,1,0),(3,1,0,'shared',5,5,1171290473621,2,1171290473621,2,2,1,0),(4,1,0,'system',5,5,1171290473621,2,1171290473621,2,2,1,0),(5,1,4,'system_global',3,5,1171290473621,2,1171290473621,2,2,1,0),(6,1,4,'system_ldap',3,5,1171290473621,2,1171290473621,2,2,1,0),(7,1,0,'user',5,5,1171290473621,2,1171290473621,2,2,1,0),(8,1,7,'projects',5,5,1171290473621,2,1171290473621,2,2,1,0),(9,1,0,'infostore',5,5,1171290473621,2,1171290473675,2,2,1,0),(21,1,1,'Calendar',2,1,1171290473675,2,1171290473675,2,1,0,1),(22,1,1,'Contacts',3,1,1171290473675,2,1171290473675,2,1,0,1),(23,1,1,'Tasks',1,1,1171290473675,2,1171290473675,2,1,0,1),(24,1,9,'Open Xchange Administrator',8,2,1171290473675,2,1171290473675,2,3,0,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `oxfolder_tree` ENABLE KEYS */;

--
-- Table structure for table `oxfolder_userfolders`
--

DROP TABLE IF EXISTS `oxfolder_userfolders`;
CREATE TABLE `oxfolder_userfolders` (
  `module` tinyint(3) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `linksite` varchar(32) NOT NULL,
  `target` varchar(32) NOT NULL,
  `img` varchar(32) NOT NULL,
  PRIMARY KEY  (`cid`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `oxfolder_userfolders`
--


/*!40000 ALTER TABLE `oxfolder_userfolders` DISABLE KEYS */;
LOCK TABLES `oxfolder_userfolders` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `oxfolder_userfolders` ENABLE KEYS */;

--
-- Table structure for table `oxfolder_userfolders_standardfolders`
--

DROP TABLE IF EXISTS `oxfolder_userfolders_standardfolders`;
CREATE TABLE `oxfolder_userfolders_standardfolders` (
  `owner` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `module` tinyint(3) unsigned NOT NULL,
  `fuid` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`owner`,`cid`,`module`,`fuid`),
  KEY `cid` (`cid`,`fuid`),
  CONSTRAINT `oxfolder_userfolders_standardfolders_ibfk_1` FOREIGN KEY (`cid`, `fuid`) REFERENCES `oxfolder_tree` (`cid`, `fuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `oxfolder_userfolders_standardfolders`
--


/*!40000 ALTER TABLE `oxfolder_userfolders_standardfolders` DISABLE KEYS */;
LOCK TABLES `oxfolder_userfolders_standardfolders` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `oxfolder_userfolders_standardfolders` ENABLE KEYS */;

--
-- Table structure for table `prg_attachment`
--

DROP TABLE IF EXISTS `prg_attachment`;
CREATE TABLE `prg_attachment` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `created_by` int(10) unsigned NOT NULL,
  `creation_date` bigint(20) NOT NULL,
  `file_mimetype` varchar(255) NOT NULL,
  `file_size` int(10) unsigned NOT NULL,
  `filename` varchar(255) NOT NULL,
  `attached` int(10) unsigned NOT NULL,
  `module` int(10) unsigned NOT NULL,
  `rtf_flag` tinyint(1) default NULL,
  `comment` varchar(255) default NULL,
  `file_id` varchar(255) NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`attached`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `prg_attachment`
--


/*!40000 ALTER TABLE `prg_attachment` DISABLE KEYS */;
LOCK TABLES `prg_attachment` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `prg_attachment` ENABLE KEYS */;

--
-- Table structure for table `prg_contacts`
--

DROP TABLE IF EXISTS `prg_contacts`;
CREATE TABLE `prg_contacts` (
  `creating_date` bigint(20) NOT NULL,
  `created_from` int(11) NOT NULL,
  `changing_date` bigint(20) NOT NULL,
  `changed_from` int(11) default NULL,
  `fid` int(11) NOT NULL,
  `cid` int(11) NOT NULL,
  `userid` int(11) default NULL,
  `pflag` int(11) default NULL,
  `timestampfield01` date default NULL,
  `timestampfield02` date default NULL,
  `intfield01` int(11) NOT NULL,
  `intfield02` int(11) default NULL,
  `intfield03` int(11) default NULL,
  `intfield04` int(11) default NULL,
  `intfield05` int(11) default NULL,
  `intfield06` int(11) default NULL,
  `intfield07` int(11) default NULL,
  `intfield08` int(11) default NULL,
  `field01` varchar(320) collate utf8_unicode_ci default NULL,
  `field02` varchar(128) collate utf8_unicode_ci default NULL,
  `field03` varchar(128) collate utf8_unicode_ci default NULL,
  `field04` varchar(128) collate utf8_unicode_ci default NULL,
  `field05` varchar(64) collate utf8_unicode_ci default NULL,
  `field06` varchar(64) collate utf8_unicode_ci default NULL,
  `field07` varchar(64) collate utf8_unicode_ci default NULL,
  `field08` varchar(64) collate utf8_unicode_ci default NULL,
  `field09` varchar(64) collate utf8_unicode_ci default NULL,
  `field10` varchar(64) collate utf8_unicode_ci default NULL,
  `field11` varchar(64) collate utf8_unicode_ci default NULL,
  `field12` varchar(64) collate utf8_unicode_ci default NULL,
  `field13` varchar(64) collate utf8_unicode_ci default NULL,
  `field14` varchar(64) collate utf8_unicode_ci default NULL,
  `field15` varchar(64) collate utf8_unicode_ci default NULL,
  `field16` varchar(64) collate utf8_unicode_ci default NULL,
  `field17` varchar(1024) collate utf8_unicode_ci default NULL,
  `field18` varchar(64) collate utf8_unicode_ci default NULL,
  `field19` varchar(128) collate utf8_unicode_ci default NULL,
  `field20` varchar(128) collate utf8_unicode_ci default NULL,
  `field21` varchar(64) collate utf8_unicode_ci default NULL,
  `field22` varchar(64) collate utf8_unicode_ci default NULL,
  `field23` varchar(64) collate utf8_unicode_ci default NULL,
  `field24` varchar(64) collate utf8_unicode_ci default NULL,
  `field25` varchar(64) collate utf8_unicode_ci default NULL,
  `field26` varchar(64) collate utf8_unicode_ci default NULL,
  `field27` varchar(64) collate utf8_unicode_ci default NULL,
  `field28` varchar(64) collate utf8_unicode_ci default NULL,
  `field29` varchar(64) collate utf8_unicode_ci default NULL,
  `field30` varchar(64) collate utf8_unicode_ci default NULL,
  `field31` varchar(64) collate utf8_unicode_ci default NULL,
  `field32` varchar(64) collate utf8_unicode_ci default NULL,
  `field33` varchar(64) collate utf8_unicode_ci default NULL,
  `field34` varchar(1024) collate utf8_unicode_ci default NULL,
  `field35` varchar(64) collate utf8_unicode_ci default NULL,
  `field36` varchar(64) collate utf8_unicode_ci default NULL,
  `field37` varchar(64) collate utf8_unicode_ci default NULL,
  `field38` varchar(64) collate utf8_unicode_ci default NULL,
  `field39` varchar(64) collate utf8_unicode_ci default NULL,
  `field40` varchar(64) collate utf8_unicode_ci default NULL,
  `field41` varchar(64) collate utf8_unicode_ci default NULL,
  `field42` varchar(64) collate utf8_unicode_ci default NULL,
  `field43` varchar(64) collate utf8_unicode_ci default NULL,
  `field44` varchar(64) collate utf8_unicode_ci default NULL,
  `field45` varchar(64) collate utf8_unicode_ci default NULL,
  `field46` varchar(64) collate utf8_unicode_ci default NULL,
  `field47` varchar(64) collate utf8_unicode_ci default NULL,
  `field48` varchar(64) collate utf8_unicode_ci default NULL,
  `field49` varchar(64) collate utf8_unicode_ci default NULL,
  `field50` varchar(64) collate utf8_unicode_ci default NULL,
  `field51` varchar(64) collate utf8_unicode_ci default NULL,
  `field52` varchar(64) collate utf8_unicode_ci default NULL,
  `field53` varchar(64) collate utf8_unicode_ci default NULL,
  `field54` varchar(64) collate utf8_unicode_ci default NULL,
  `field55` varchar(64) collate utf8_unicode_ci default NULL,
  `field56` varchar(64) collate utf8_unicode_ci default NULL,
  `field57` varchar(64) collate utf8_unicode_ci default NULL,
  `field58` varchar(64) collate utf8_unicode_ci default NULL,
  `field59` varchar(64) collate utf8_unicode_ci default NULL,
  `field60` varchar(64) collate utf8_unicode_ci default NULL,
  `field61` varchar(64) collate utf8_unicode_ci default NULL,
  `field62` varchar(64) collate utf8_unicode_ci default NULL,
  `field63` varchar(64) collate utf8_unicode_ci default NULL,
  `field64` varchar(64) collate utf8_unicode_ci default NULL,
  `field65` varchar(256) collate utf8_unicode_ci default NULL,
  `field66` varchar(256) collate utf8_unicode_ci default NULL,
  `field67` varchar(256) collate utf8_unicode_ci default NULL,
  `field68` varchar(64) collate utf8_unicode_ci default NULL,
  `field69` varchar(64) collate utf8_unicode_ci default NULL,
  `field70` varchar(64) collate utf8_unicode_ci default NULL,
  `field71` varchar(64) collate utf8_unicode_ci default NULL,
  `field72` varchar(64) collate utf8_unicode_ci default NULL,
  `field73` varchar(64) collate utf8_unicode_ci default NULL,
  `field74` varchar(64) collate utf8_unicode_ci default NULL,
  `field75` varchar(64) collate utf8_unicode_ci default NULL,
  `field76` varchar(64) collate utf8_unicode_ci default NULL,
  `field77` varchar(64) collate utf8_unicode_ci default NULL,
  `field78` varchar(64) collate utf8_unicode_ci default NULL,
  `field79` varchar(64) collate utf8_unicode_ci default NULL,
  `field80` varchar(64) collate utf8_unicode_ci default NULL,
  `field81` varchar(64) collate utf8_unicode_ci default NULL,
  `field82` varchar(64) collate utf8_unicode_ci default NULL,
  `field83` varchar(64) collate utf8_unicode_ci default NULL,
  `field84` varchar(64) collate utf8_unicode_ci default NULL,
  `field85` varchar(64) collate utf8_unicode_ci default NULL,
  `field86` varchar(64) collate utf8_unicode_ci default NULL,
  `field87` varchar(64) collate utf8_unicode_ci default NULL,
  `field88` varchar(64) collate utf8_unicode_ci default NULL,
  `field89` varchar(64) collate utf8_unicode_ci default NULL,
  `field90` varchar(64) collate utf8_unicode_ci default NULL,
  `field91` varchar(64) collate utf8_unicode_ci default NULL,
  `field92` varchar(64) collate utf8_unicode_ci default NULL,
  `field93` varchar(64) collate utf8_unicode_ci default NULL,
  `field94` varchar(64) collate utf8_unicode_ci default NULL,
  `field95` varchar(64) collate utf8_unicode_ci default NULL,
  `field96` varchar(64) collate utf8_unicode_ci default NULL,
  `field97` varchar(64) collate utf8_unicode_ci default NULL,
  `field98` varchar(64) collate utf8_unicode_ci default NULL,
  `field99` varchar(64) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`intfield01`),
  KEY `created_from` (`created_from`),
  KEY `changing_date` (`changing_date`),
  KEY `userid` (`userid`),
  KEY `cid` (`cid`,`fid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `prg_contacts`
--


/*!40000 ALTER TABLE `prg_contacts` DISABLE KEYS */;
LOCK TABLES `prg_contacts` WRITE;
INSERT INTO `prg_contacts` VALUES (1171290473589,2,1171290473589,NULL,6,1,2,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Open Xchange Administrator','Admin','Open-Xchange',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'oxadmin@@MAIL_DOMAIN@',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `prg_contacts` ENABLE KEYS */;

--
-- Table structure for table `prg_contacts_image`
--

DROP TABLE IF EXISTS `prg_contacts_image`;
CREATE TABLE `prg_contacts_image` (
  `intfield01` int(11) NOT NULL,
  `image1` mediumblob,
  `changing_date` bigint(20) NOT NULL,
  `mime_type` varchar(32) NOT NULL,
  `cid` int(11) NOT NULL,
  PRIMARY KEY  (`cid`,`intfield01`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `prg_contacts_image`
--


/*!40000 ALTER TABLE `prg_contacts_image` DISABLE KEYS */;
LOCK TABLES `prg_contacts_image` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `prg_contacts_image` ENABLE KEYS */;

--
-- Table structure for table `prg_contacts_linkage`
--

DROP TABLE IF EXISTS `prg_contacts_linkage`;
CREATE TABLE `prg_contacts_linkage` (
  `intfield01` int(11) NOT NULL,
  `intfield02` int(11) NOT NULL,
  `field01` varchar(320) collate utf8_unicode_ci default NULL,
  `field02` varchar(320) collate utf8_unicode_ci default NULL,
  `cid` int(11) NOT NULL,
  KEY `intfield01` (`intfield01`,`intfield02`,`cid`),
  KEY `cid` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `prg_contacts_linkage`
--


/*!40000 ALTER TABLE `prg_contacts_linkage` DISABLE KEYS */;
LOCK TABLES `prg_contacts_linkage` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `prg_contacts_linkage` ENABLE KEYS */;

--
-- Table structure for table `prg_date_rights`
--

DROP TABLE IF EXISTS `prg_date_rights`;
CREATE TABLE `prg_date_rights` (
  `object_id` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `type` int(10) unsigned NOT NULL,
  `ma` varchar(64) default NULL,
  `dn` varchar(64) default NULL,
  PRIMARY KEY  (`cid`,`object_id`,`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `prg_date_rights`
--


/*!40000 ALTER TABLE `prg_date_rights` DISABLE KEYS */;
LOCK TABLES `prg_date_rights` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `prg_date_rights` ENABLE KEYS */;

--
-- Table structure for table `prg_dates`
--

DROP TABLE IF EXISTS `prg_dates`;
CREATE TABLE `prg_dates` (
  `creating_date` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `created_from` int(10) unsigned NOT NULL,
  `changing_date` bigint(20) NOT NULL,
  `changed_from` int(10) unsigned NOT NULL,
  `fid` int(10) unsigned NOT NULL,
  `pflag` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `timestampfield01` datetime NOT NULL,
  `timestampfield02` datetime NOT NULL,
  `timezone` varchar(64) collate utf8_unicode_ci NOT NULL,
  `intfield01` int(10) unsigned NOT NULL,
  `intfield02` int(10) unsigned default NULL,
  `intfield03` int(10) unsigned default NULL,
  `intfield04` int(10) unsigned default NULL,
  `intfield05` int(10) unsigned default NULL,
  `intfield06` int(10) unsigned NOT NULL,
  `intfield07` int(10) unsigned default NULL,
  `intfield08` int(10) unsigned default NULL,
  `field01` varchar(255) collate utf8_unicode_ci default NULL,
  `field02` varchar(255) collate utf8_unicode_ci default NULL,
  `field04` text collate utf8_unicode_ci,
  `field06` varchar(64) collate utf8_unicode_ci default NULL,
  `field07` varchar(255) collate utf8_unicode_ci default NULL,
  `field08` varchar(255) collate utf8_unicode_ci default NULL,
  `field09` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`intfield01`),
  KEY `timestampfield01` (`timestampfield01`),
  KEY `timestampfield02` (`timestampfield02`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `prg_dates`
--


/*!40000 ALTER TABLE `prg_dates` DISABLE KEYS */;
LOCK TABLES `prg_dates` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `prg_dates` ENABLE KEYS */;

--
-- Table structure for table `prg_dates_members`
--

DROP TABLE IF EXISTS `prg_dates_members`;
CREATE TABLE `prg_dates_members` (
  `object_id` int(11) NOT NULL default '0',
  `member_uid` int(11) NOT NULL default '0',
  `confirm` int(10) unsigned NOT NULL,
  `reason` varchar(255) collate utf8_unicode_ci default NULL,
  `pfid` int(11) default NULL,
  `reminder` int(10) unsigned default NULL,
  `cid` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`object_id`,`member_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `prg_dates_members`
--


/*!40000 ALTER TABLE `prg_dates_members` DISABLE KEYS */;
LOCK TABLES `prg_dates_members` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `prg_dates_members` ENABLE KEYS */;

--
-- Table structure for table `prg_dlist`
--

DROP TABLE IF EXISTS `prg_dlist`;
CREATE TABLE `prg_dlist` (
  `intfield01` int(11) NOT NULL,
  `intfield02` int(11) default NULL,
  `intfield03` int(11) default NULL,
  `intfield04` int(11) default NULL,
  `field01` varchar(320) collate utf8_unicode_ci default NULL,
  `field02` varchar(128) collate utf8_unicode_ci default NULL,
  `field03` varchar(128) collate utf8_unicode_ci default NULL,
  `field04` varchar(128) collate utf8_unicode_ci default NULL,
  `cid` int(11) default NULL,
  KEY `intfield01` (`intfield01`,`cid`),
  KEY `intfield01_2` (`intfield01`,`intfield02`,`intfield03`,`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `prg_dlist`
--


/*!40000 ALTER TABLE `prg_dlist` DISABLE KEYS */;
LOCK TABLES `prg_dlist` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `prg_dlist` ENABLE KEYS */;

--
-- Table structure for table `prg_links`
--

DROP TABLE IF EXISTS `prg_links`;
CREATE TABLE `prg_links` (
  `firstid` int(11) NOT NULL,
  `firstmodule` int(11) NOT NULL,
  `firstfolder` int(11) NOT NULL,
  `secondid` int(11) NOT NULL,
  `secondmodule` int(11) NOT NULL,
  `secondfolder` int(11) NOT NULL,
  `cid` int(11) NOT NULL,
  `last_modified` bigint(20) default NULL,
  `created_by` int(11) default NULL,
  KEY `firstid` (`firstid`),
  KEY `secondid` (`secondid`),
  KEY `cid` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `prg_links`
--


/*!40000 ALTER TABLE `prg_links` DISABLE KEYS */;
LOCK TABLES `prg_links` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `prg_links` ENABLE KEYS */;

--
-- Table structure for table `reminder`
--

DROP TABLE IF EXISTS `reminder`;
CREATE TABLE `reminder` (
  `object_id` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `last_modified` bigint(20) unsigned default NULL,
  `target_id` varchar(255) collate utf8_unicode_ci NOT NULL,
  `module` tinyint(3) unsigned NOT NULL,
  `userid` int(10) unsigned NOT NULL,
  `alarm` datetime NOT NULL,
  `recurrence` tinyint(4) NOT NULL,
  `description` varchar(1028) collate utf8_unicode_ci default NULL,
  `folder` varchar(1028) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`object_id`),
  UNIQUE KEY `reminder_unique` (`cid`,`target_id`,`module`,`userid`),
  KEY `cid` (`cid`,`target_id`),
  KEY `cid_2` (`cid`,`userid`,`alarm`),
  KEY `cid_3` (`cid`,`userid`,`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `reminder`
--


/*!40000 ALTER TABLE `reminder` DISABLE KEYS */;
LOCK TABLES `reminder` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `reminder` ENABLE KEYS */;

--
-- Table structure for table `resource`
--

DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `identifier` varchar(128) collate utf8_unicode_ci NOT NULL,
  `displayName` varchar(128) collate utf8_unicode_ci NOT NULL,
  `mail` varchar(256) collate utf8_unicode_ci default NULL,
  `available` tinyint(1) NOT NULL,
  `description` varchar(255) collate utf8_unicode_ci default NULL,
  `lastModified` bigint(20) NOT NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `resource`
--


/*!40000 ALTER TABLE `resource` DISABLE KEYS */;
LOCK TABLES `resource` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `resource` ENABLE KEYS */;

--
-- Table structure for table `resource_group`
--

DROP TABLE IF EXISTS `resource_group`;
CREATE TABLE `resource_group` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `identifier` varchar(128) collate utf8_unicode_ci NOT NULL,
  `displayName` varchar(128) collate utf8_unicode_ci NOT NULL,
  `available` tinyint(1) NOT NULL,
  `description` varchar(255) collate utf8_unicode_ci default NULL,
  `lastModified` bigint(20) NOT NULL,
  PRIMARY KEY  (`cid`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `resource_group`
--


/*!40000 ALTER TABLE `resource_group` DISABLE KEYS */;
LOCK TABLES `resource_group` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `resource_group` ENABLE KEYS */;

--
-- Table structure for table `resource_group_member`
--

DROP TABLE IF EXISTS `resource_group_member`;
CREATE TABLE `resource_group_member` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `member` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`id`,`member`),
  KEY `cid` (`cid`,`member`),
  CONSTRAINT `resource_group_member_ibfk_1` FOREIGN KEY (`cid`, `id`) REFERENCES `resource_group` (`cid`, `id`),
  CONSTRAINT `resource_group_member_ibfk_2` FOREIGN KEY (`cid`, `member`) REFERENCES `resource` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `resource_group_member`
--


/*!40000 ALTER TABLE `resource_group_member` DISABLE KEYS */;
LOCK TABLES `resource_group_member` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `resource_group_member` ENABLE KEYS */;

--
-- Table structure for table `sequence_attachment`
--

DROP TABLE IF EXISTS `sequence_attachment`;
CREATE TABLE `sequence_attachment` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_attachment`
--


/*!40000 ALTER TABLE `sequence_attachment` DISABLE KEYS */;
LOCK TABLES `sequence_attachment` WRITE;
INSERT INTO `sequence_attachment` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_attachment` ENABLE KEYS */;

--
-- Table structure for table `sequence_calendar`
--

DROP TABLE IF EXISTS `sequence_calendar`;
CREATE TABLE `sequence_calendar` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_calendar`
--


/*!40000 ALTER TABLE `sequence_calendar` DISABLE KEYS */;
LOCK TABLES `sequence_calendar` WRITE;
INSERT INTO `sequence_calendar` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_calendar` ENABLE KEYS */;

--
-- Table structure for table `sequence_contact`
--

DROP TABLE IF EXISTS `sequence_contact`;
CREATE TABLE `sequence_contact` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_contact`
--


/*!40000 ALTER TABLE `sequence_contact` DISABLE KEYS */;
LOCK TABLES `sequence_contact` WRITE;
INSERT INTO `sequence_contact` VALUES (1,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_contact` ENABLE KEYS */;

--
-- Table structure for table `sequence_folder`
--

DROP TABLE IF EXISTS `sequence_folder`;
CREATE TABLE `sequence_folder` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_folder`
--


/*!40000 ALTER TABLE `sequence_folder` DISABLE KEYS */;
LOCK TABLES `sequence_folder` WRITE;
INSERT INTO `sequence_folder` VALUES (1,24);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_folder` ENABLE KEYS */;

--
-- Table structure for table `sequence_forum`
--

DROP TABLE IF EXISTS `sequence_forum`;
CREATE TABLE `sequence_forum` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_forum`
--


/*!40000 ALTER TABLE `sequence_forum` DISABLE KEYS */;
LOCK TABLES `sequence_forum` WRITE;
INSERT INTO `sequence_forum` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_forum` ENABLE KEYS */;

--
-- Table structure for table `sequence_gui_setting`
--

DROP TABLE IF EXISTS `sequence_gui_setting`;
CREATE TABLE `sequence_gui_setting` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_gui_setting`
--


/*!40000 ALTER TABLE `sequence_gui_setting` DISABLE KEYS */;
LOCK TABLES `sequence_gui_setting` WRITE;
INSERT INTO `sequence_gui_setting` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_gui_setting` ENABLE KEYS */;

--
-- Table structure for table `sequence_ical`
--

DROP TABLE IF EXISTS `sequence_ical`;
CREATE TABLE `sequence_ical` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_ical`
--


/*!40000 ALTER TABLE `sequence_ical` DISABLE KEYS */;
LOCK TABLES `sequence_ical` WRITE;
INSERT INTO `sequence_ical` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_ical` ENABLE KEYS */;

--
-- Table structure for table `sequence_id`
--

DROP TABLE IF EXISTS `sequence_id`;
CREATE TABLE `sequence_id` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_id`
--


/*!40000 ALTER TABLE `sequence_id` DISABLE KEYS */;
LOCK TABLES `sequence_id` WRITE;
INSERT INTO `sequence_id` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_id` ENABLE KEYS */;

--
-- Table structure for table `sequence_infostore`
--

DROP TABLE IF EXISTS `sequence_infostore`;
CREATE TABLE `sequence_infostore` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_infostore`
--


/*!40000 ALTER TABLE `sequence_infostore` DISABLE KEYS */;
LOCK TABLES `sequence_infostore` WRITE;
INSERT INTO `sequence_infostore` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_infostore` ENABLE KEYS */;

--
-- Table structure for table `sequence_pinboard`
--

DROP TABLE IF EXISTS `sequence_pinboard`;
CREATE TABLE `sequence_pinboard` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_pinboard`
--


/*!40000 ALTER TABLE `sequence_pinboard` DISABLE KEYS */;
LOCK TABLES `sequence_pinboard` WRITE;
INSERT INTO `sequence_pinboard` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_pinboard` ENABLE KEYS */;

--
-- Table structure for table `sequence_principal`
--

DROP TABLE IF EXISTS `sequence_principal`;
CREATE TABLE `sequence_principal` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_principal`
--


/*!40000 ALTER TABLE `sequence_principal` DISABLE KEYS */;
LOCK TABLES `sequence_principal` WRITE;
INSERT INTO `sequence_principal` VALUES (1,2);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_principal` ENABLE KEYS */;

--
-- Table structure for table `sequence_project`
--

DROP TABLE IF EXISTS `sequence_project`;
CREATE TABLE `sequence_project` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_project`
--


/*!40000 ALTER TABLE `sequence_project` DISABLE KEYS */;
LOCK TABLES `sequence_project` WRITE;
INSERT INTO `sequence_project` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_project` ENABLE KEYS */;

--
-- Table structure for table `sequence_reminder`
--

DROP TABLE IF EXISTS `sequence_reminder`;
CREATE TABLE `sequence_reminder` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_reminder`
--


/*!40000 ALTER TABLE `sequence_reminder` DISABLE KEYS */;
LOCK TABLES `sequence_reminder` WRITE;
INSERT INTO `sequence_reminder` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_reminder` ENABLE KEYS */;

--
-- Table structure for table `sequence_resource`
--

DROP TABLE IF EXISTS `sequence_resource`;
CREATE TABLE `sequence_resource` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_resource`
--


/*!40000 ALTER TABLE `sequence_resource` DISABLE KEYS */;
LOCK TABLES `sequence_resource` WRITE;
INSERT INTO `sequence_resource` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_resource` ENABLE KEYS */;

--
-- Table structure for table `sequence_resource_group`
--

DROP TABLE IF EXISTS `sequence_resource_group`;
CREATE TABLE `sequence_resource_group` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_resource_group`
--


/*!40000 ALTER TABLE `sequence_resource_group` DISABLE KEYS */;
LOCK TABLES `sequence_resource_group` WRITE;
INSERT INTO `sequence_resource_group` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_resource_group` ENABLE KEYS */;

--
-- Table structure for table `sequence_task`
--

DROP TABLE IF EXISTS `sequence_task`;
CREATE TABLE `sequence_task` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_task`
--


/*!40000 ALTER TABLE `sequence_task` DISABLE KEYS */;
LOCK TABLES `sequence_task` WRITE;
INSERT INTO `sequence_task` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_task` ENABLE KEYS */;

--
-- Table structure for table `sequence_webdav`
--

DROP TABLE IF EXISTS `sequence_webdav`;
CREATE TABLE `sequence_webdav` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `sequence_webdav`
--


/*!40000 ALTER TABLE `sequence_webdav` DISABLE KEYS */;
LOCK TABLES `sequence_webdav` WRITE;
INSERT INTO `sequence_webdav` VALUES (1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequence_webdav` ENABLE KEYS */;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `private` tinyint(1) NOT NULL,
  `creating_date` datetime NOT NULL,
  `last_modified` bigint(20) NOT NULL,
  `created_from` int(10) unsigned NOT NULL,
  `changed_from` int(10) unsigned default NULL,
  `start` datetime default NULL,
  `end` datetime default NULL,
  `completed` datetime default NULL,
  `title` varchar(128) collate utf8_unicode_ci default NULL,
  `description` text collate utf8_unicode_ci,
  `state` int(11) default NULL,
  `priority` int(11) default NULL,
  `progress` int(11) default NULL,
  `categories` varchar(255) collate utf8_unicode_ci default NULL,
  `project` int(10) unsigned default NULL,
  `target_duration` bigint(20) default NULL,
  `actual_duration` bigint(20) default NULL,
  `target_costs` float default NULL,
  `actual_costs` float default NULL,
  `currency` varchar(10) collate utf8_unicode_ci default NULL,
  `trip_meter` varchar(255) collate utf8_unicode_ci default NULL,
  `billing` varchar(255) collate utf8_unicode_ci default NULL,
  `companies` varchar(255) collate utf8_unicode_ci default NULL,
  `color_label` tinyint(3) unsigned default NULL,
  `recurrence_type` tinyint(3) unsigned NOT NULL,
  `recurrence_interval` int(10) unsigned default NULL,
  `recurrence_days` tinyint(3) unsigned default NULL,
  `recurrence_dayinmonth` tinyint(3) unsigned default NULL,
  `recurrence_month` tinyint(3) unsigned default NULL,
  `recurrence_until` datetime default NULL,
  `recurrence_count` smallint(5) unsigned default NULL,
  `number_of_attachments` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`id`),
  KEY `cid` (`cid`,`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `task`
--


/*!40000 ALTER TABLE `task` DISABLE KEYS */;
LOCK TABLES `task` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `task` ENABLE KEYS */;

--
-- Table structure for table `task_eparticipant`
--

DROP TABLE IF EXISTS `task_eparticipant`;
CREATE TABLE `task_eparticipant` (
  `cid` int(10) unsigned NOT NULL,
  `task` int(10) unsigned NOT NULL,
  `mail` varchar(255) collate utf8_unicode_ci NOT NULL,
  `display_name` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`task`,`mail`),
  CONSTRAINT `task_eparticipant_ibfk_1` FOREIGN KEY (`cid`, `task`) REFERENCES `task` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `task_eparticipant`
--


/*!40000 ALTER TABLE `task_eparticipant` DISABLE KEYS */;
LOCK TABLES `task_eparticipant` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `task_eparticipant` ENABLE KEYS */;

--
-- Table structure for table `task_folder`
--

DROP TABLE IF EXISTS `task_folder`;
CREATE TABLE `task_folder` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `folder` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`id`,`folder`),
  KEY `cid` (`cid`,`folder`),
  CONSTRAINT `task_folder_ibfk_1` FOREIGN KEY (`cid`, `id`) REFERENCES `task` (`cid`, `id`),
  CONSTRAINT `task_folder_ibfk_2` FOREIGN KEY (`cid`, `folder`) REFERENCES `oxfolder_tree` (`cid`, `fuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `task_folder`
--


/*!40000 ALTER TABLE `task_folder` DISABLE KEYS */;
LOCK TABLES `task_folder` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `task_folder` ENABLE KEYS */;

--
-- Table structure for table `task_participant`
--

DROP TABLE IF EXISTS `task_participant`;
CREATE TABLE `task_participant` (
  `cid` int(10) unsigned NOT NULL,
  `task` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `group_id` int(10) unsigned default NULL,
  `accepted` tinyint(3) unsigned NOT NULL,
  `description` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`cid`,`task`,`user`),
  KEY `cid` (`cid`,`user`),
  CONSTRAINT `task_participant_ibfk_1` FOREIGN KEY (`cid`, `task`) REFERENCES `task` (`cid`, `id`),
  CONSTRAINT `task_participant_ibfk_2` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `task_participant`
--


/*!40000 ALTER TABLE `task_participant` DISABLE KEYS */;
LOCK TABLES `task_participant` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `task_participant` ENABLE KEYS */;

--
-- Table structure for table `task_removedparticipant`
--

DROP TABLE IF EXISTS `task_removedparticipant`;
CREATE TABLE `task_removedparticipant` (
  `cid` int(10) unsigned NOT NULL,
  `task` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `group_id` int(10) unsigned default NULL,
  `accepted` tinyint(3) unsigned NOT NULL,
  `description` text,
  `folder` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`task`,`user`),
  KEY `cid` (`cid`,`folder`),
  KEY `cid_2` (`cid`,`user`),
  CONSTRAINT `task_removedparticipant_ibfk_1` FOREIGN KEY (`cid`, `task`) REFERENCES `task` (`cid`, `id`),
  CONSTRAINT `task_removedparticipant_ibfk_2` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `task_removedparticipant`
--


/*!40000 ALTER TABLE `task_removedparticipant` DISABLE KEYS */;
LOCK TABLES `task_removedparticipant` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `task_removedparticipant` ENABLE KEYS */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `imapServer` varchar(128) collate utf8_unicode_ci default NULL,
  `imapLogin` varchar(128) collate utf8_unicode_ci default NULL,
  `mail` varchar(256) collate utf8_unicode_ci NOT NULL,
  `mailDomain` varchar(128) collate utf8_unicode_ci default NULL,
  `mailEnabled` tinyint(1) NOT NULL,
  `preferredLanguage` varchar(10) collate utf8_unicode_ci NOT NULL,
  `shadowLastChange` int(11) NOT NULL,
  `smtpServer` varchar(128) collate utf8_unicode_ci default NULL,
  `timeZone` varchar(128) collate utf8_unicode_ci NOT NULL,
  `userPassword` varchar(128) collate utf8_unicode_ci default NULL,
  `contactId` int(10) unsigned NOT NULL,
  `passwordMech` varchar(32) collate utf8_unicode_ci NOT NULL default '{CRYPT}',
  `uidNumber` int(10) unsigned NOT NULL default '5000',
  `gidNumber` int(10) unsigned NOT NULL default '5000',
  `homeDirectory` varchar(128) collate utf8_unicode_ci NOT NULL default '/tmp/foo',
  `loginShell` varchar(128) collate utf8_unicode_ci NOT NULL default '/bin/bash',
  PRIMARY KEY  (`cid`,`id`),
  KEY `mail` (`mail`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `user`
--


/*!40000 ALTER TABLE `user` DISABLE KEYS */;
LOCK TABLES `user` WRITE;
INSERT INTO `user` VALUES (1,2,'localhost',NULL,'oxadmin@@MAIL_DOMAIN@',NULL,1,'en_US',-1,'localhost','Europe/Berlin','@ADMINPW@',1,'{CRYPT}',5000,5000,'/tmp/foo','/bin/bash');
UNLOCK TABLES;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;

--
-- Table structure for table `user_attribute`
--

DROP TABLE IF EXISTS `user_attribute`;
CREATE TABLE `user_attribute` (
  `cid` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `name` varchar(128) collate utf8_unicode_ci NOT NULL,
  `value` varchar(128) collate utf8_unicode_ci NOT NULL,
  KEY `cid` (`cid`,`name`,`value`),
  KEY `cid_2` (`cid`,`id`),
  CONSTRAINT `user_attribute_ibfk_1` FOREIGN KEY (`cid`, `id`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `user_attribute`
--


/*!40000 ALTER TABLE `user_attribute` DISABLE KEYS */;
LOCK TABLES `user_attribute` WRITE;
INSERT INTO `user_attribute` VALUES (1,2,'alias','oxadmin@@MAIL_DOMAIN@');
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_attribute` ENABLE KEYS */;

--
-- Table structure for table `user_configuration`
--

DROP TABLE IF EXISTS `user_configuration`;
CREATE TABLE `user_configuration` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `permissions` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`user`),
  CONSTRAINT `user_configuration_ibfk_1` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `user_configuration`
--


/*!40000 ALTER TABLE `user_configuration` DISABLE KEYS */;
LOCK TABLES `user_configuration` WRITE;
INSERT INTO `user_configuration` VALUES (1,2,262143);
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_configuration` ENABLE KEYS */;

--
-- Table structure for table `user_setting`
--

DROP TABLE IF EXISTS `user_setting`;
CREATE TABLE `user_setting` (
  `cid` int(10) unsigned NOT NULL,
  `user_id` int(10) unsigned NOT NULL,
  `path_id` int(10) unsigned NOT NULL,
  `value` text,
  PRIMARY KEY  (`cid`,`user_id`,`path_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `user_setting`
--


/*!40000 ALTER TABLE `user_setting` DISABLE KEYS */;
LOCK TABLES `user_setting` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_setting` ENABLE KEYS */;

--
-- Table structure for table `user_setting_admin`
--

DROP TABLE IF EXISTS `user_setting_admin`;
CREATE TABLE `user_setting_admin` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`user`),
  CONSTRAINT `user_setting_admin_ibfk_1` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `user_setting_admin`
--


/*!40000 ALTER TABLE `user_setting_admin` DISABLE KEYS */;
LOCK TABLES `user_setting_admin` WRITE;
INSERT INTO `user_setting_admin` VALUES (1,2);
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_setting_admin` ENABLE KEYS */;

--
-- Table structure for table `user_setting_mail`
--

DROP TABLE IF EXISTS `user_setting_mail`;
CREATE TABLE `user_setting_mail` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `bits` int(32) unsigned default '0',
  `send_addr` varchar(256) character set utf8 collate utf8_unicode_ci NOT NULL,
  `reply_to_addr` varchar(256) character set utf8 collate utf8_unicode_ci default NULL,
  `msg_format` tinyint(4) unsigned default '1',
  `display_msg_headers` varchar(256) character set utf8 collate utf8_unicode_ci default NULL,
  `auto_linebreak` int(10) unsigned default '80',
  `std_trash` varchar(128) character set utf8 collate utf8_unicode_ci NOT NULL,
  `std_sent` varchar(128) character set utf8 collate utf8_unicode_ci NOT NULL,
  `std_drafts` varchar(128) character set utf8 collate utf8_unicode_ci NOT NULL,
  `std_spam` varchar(128) character set utf8 collate utf8_unicode_ci NOT NULL,
  `upload_quota` int(10) unsigned default '0',
  `upload_quota_per_file` int(10) unsigned default '0',
  PRIMARY KEY  (`cid`,`user`),
  CONSTRAINT `user_setting_mail_ibfk_1` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `user_setting_mail`
--


/*!40000 ALTER TABLE `user_setting_mail` DISABLE KEYS */;
LOCK TABLES `user_setting_mail` WRITE;
INSERT INTO `user_setting_mail` VALUES (1,2,768,'oxadmin@@MAIL_DOMAIN@',NULL,1,NULL,80,'Trash','Sent Items','Drafts','Spam',0,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_setting_mail` ENABLE KEYS */;

--
-- Table structure for table `user_setting_mail_signature`
--

DROP TABLE IF EXISTS `user_setting_mail_signature`;
CREATE TABLE `user_setting_mail_signature` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `id` varchar(64) character set utf8 collate utf8_unicode_ci NOT NULL,
  `signature` varchar(1024) character set utf8 collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`cid`,`user`,`id`),
  KEY `cid` (`cid`,`user`),
  CONSTRAINT `user_setting_mail_signature_ibfk_1` FOREIGN KEY (`cid`, `user`) REFERENCES `user_setting_mail` (`cid`, `user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `user_setting_mail_signature`
--


/*!40000 ALTER TABLE `user_setting_mail_signature` DISABLE KEYS */;
LOCK TABLES `user_setting_mail_signature` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_setting_mail_signature` ENABLE KEYS */;

--
-- Table structure for table `user_setting_spellcheck`
--

DROP TABLE IF EXISTS `user_setting_spellcheck`;
CREATE TABLE `user_setting_spellcheck` (
  `cid` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  `user_dic` text character set utf8 collate utf8_unicode_ci,
  PRIMARY KEY  (`cid`,`user`),
  CONSTRAINT `user_setting_spellcheck_ibfk_1` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `user_setting_spellcheck`
--


/*!40000 ALTER TABLE `user_setting_spellcheck` DISABLE KEYS */;
LOCK TABLES `user_setting_spellcheck` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_setting_spellcheck` ENABLE KEYS */;

--
-- Table structure for table `vcard_ids`
--

DROP TABLE IF EXISTS `vcard_ids`;
CREATE TABLE `vcard_ids` (
  `object_id` int(11) NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `principal_id` int(11) default NULL,
  `client_id` text,
  `target_object_id` int(11) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `vcard_ids`
--


/*!40000 ALTER TABLE `vcard_ids` DISABLE KEYS */;
LOCK TABLES `vcard_ids` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `vcard_ids` ENABLE KEYS */;

--
-- Table structure for table `vcard_principal`
--

DROP TABLE IF EXISTS `vcard_principal`;
CREATE TABLE `vcard_principal` (
  `object_id` int(11) NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `principal` text,
  `contactfolder` int(11) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `vcard_principal`
--


/*!40000 ALTER TABLE `vcard_principal` DISABLE KEYS */;
LOCK TABLES `vcard_principal` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `vcard_principal` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

