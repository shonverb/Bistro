-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: bistro
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `full_name` varchar(100) DEFAULT NULL,
  `subscriber_id` int NOT NULL,
  `username` varchar(100) DEFAULT NULL,
  `phone_number` varchar(10) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `status` enum('CLIENT','EMPLOYEE','MANAGER') DEFAULT NULL,
  PRIMARY KEY (`subscriber_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('dean zarhi',1,'deanza','0501234123','dz@gmail.com','CLIENT'),('refael biton',2,'rafa','0547909190','ref@gmail.com','EMPLOYEE'),('hod vakrat',3,'compi','0506421020','vakrat@gmail.com','MANAGER'),('Yossi Cohen',4001,'yossi_c','0521234567','yossi.c@gmail.com','CLIENT'),('Dana Levi',4002,'danale','0547654321','dana.l@walla.co.il','CLIENT'),('Amit Tzur',4003,'amitush','0509988776','amit.tz@gmail.com','CLIENT'),('Noa Kirelsson',4004,'unicorn','0524455667','noa.pop@music.com','CLIENT'),('Barak Obama',4005,'potus44','0501112222','barak@usa.gov','CLIENT'),('Sarah Connor',4006,'skynet','0543334444','sarah@resistance.com','CLIENT'),('Ron Weas',4007,'ronald','0526667777','ron@magic.uk','EMPLOYEE'),('Gal Gadot',4008,'wonder','0508889999','gal@hollywood.com','CLIENT'),('snajcj aksd',130823,'bla','0506421020','hod@vak.com',NULL),('leon leonid',197392,'compipompi','0506421020','hod@val.com','CLIENT'),('kafj aksfj',421827,'klef','0507894561','name@walla.com','CLIENT'),('shon verbitzki',429173,'verb','050555111','shonson',NULL),('bla blabla',843428,'king','0501234567','name@gmail.com','CLIENT');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-15  9:52:56
