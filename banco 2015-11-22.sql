/*
SQLyog Community v12.09 (32 bit)
MySQL - 5.5.46-0+deb8u1 : Database - UNISC_Rides
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`UNISC_Rides` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;

USE `UNISC_Rides`;

/*Table structure for table `Allocation` */

DROP TABLE IF EXISTS `Allocation`;

CREATE TABLE `Allocation` (
  `person` int(10) unsigned NOT NULL,
  `origin` int(10) unsigned NOT NULL,
  `offer` int(10) unsigned NOT NULL,
  `status_` tinytext COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*Table structure for table `Login` */

DROP TABLE IF EXISTS `Login`;

CREATE TABLE `Login` (
  `person` int(10) unsigned NOT NULL,
  `username` longtext COLLATE utf8_unicode_ci NOT NULL,
  `password_` longtext COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`person`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*Table structure for table `Match_` */

DROP TABLE IF EXISTS `Match_`;

CREATE TABLE `Match_` (
  `offer` int(10) unsigned NOT NULL,
  `origin` int(10) unsigned NOT NULL,
  `in_` tinyint(1) NOT NULL,
  PRIMARY KEY (`offer`,`origin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*Table structure for table `Message` */

DROP TABLE IF EXISTS `Message`;

CREATE TABLE `Message` (
  `sender` int(10) unsigned NOT NULL,
  `receiver` int(10) unsigned NOT NULL,
  `date_` date NOT NULL,
  `time_` time NOT NULL,
  `message` longtext COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*Table structure for table `Offer` */

DROP TABLE IF EXISTS `Offer`;

CREATE TABLE `Offer` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `person` int(11) unsigned NOT NULL,
  `origin` int(10) unsigned NOT NULL,
  `distance` double unsigned NOT NULL,
  `availableSeats` tinyint(4) NOT NULL,
  `remainingSeats` tinyint(4) NOT NULL,
  `weekDays` tinyint(4) NOT NULL,
  `shift` tinytext COLLATE utf8_unicode_ci NOT NULL,
  `insertionDate` date NOT NULL,
  `promiscuous` tinyint(1) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`,`person`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*Table structure for table `Origin` */

DROP TABLE IF EXISTS `Origin`;

CREATE TABLE `Origin` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `person` int(11) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `nickname` longtext COLLATE utf8_unicode_ci,
  `street` longtext COLLATE utf8_unicode_ci,
  `number` int(11) DEFAULT NULL,
  `neighborhood` longtext COLLATE utf8_unicode_ci,
  `city` longtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*Table structure for table `Person` */

DROP TABLE IF EXISTS `Person`;

CREATE TABLE `Person` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name_` longtext COLLATE utf8_unicode_ci NOT NULL,
  `phone` tinytext COLLATE utf8_unicode_ci,
  `email` longtext COLLATE utf8_unicode_ci,
  `IP` tinytext COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*Table structure for table `RouteBox` */

DROP TABLE IF EXISTS `RouteBox`;

CREATE TABLE `RouteBox` (
  `offer` int(10) unsigned NOT NULL,
  `northEastLatitude` double NOT NULL,
  `northEastLongitude` double NOT NULL,
  `southWestLatitude` double NOT NULL,
  `southWestLongitude` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
