-- ECOSPOT FULL DATABASE DUMP
-- Target: MySQL/XAMPP
-- Database: projetdev

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- 1. Setup Database
DROP DATABASE IF EXISTS `projetdev`;
CREATE DATABASE `projetdev`;
USE `projetdev`;

-- 2. User Table
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) DEFAULT 'USER',
  `avatar_style` varchar(50) DEFAULT 'avataaars',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Category Table
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Blog Table
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `author` varchar(255) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `published_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `category_id` int(11) DEFAULT NULL,
  `views` int(11) DEFAULT 0,
  `likes_count` int(11) DEFAULT 0,
  `dislikes_count` int(11) DEFAULT 0,
  `reading_time` int(11) DEFAULT 5,
  `comments_count` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `fk_blog_category` (`category_id`),
  CONSTRAINT `fk_blog_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Event Table
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `slug` varchar(128) NOT NULL,
  `description` text NOT NULL,
  `capacity` int(11) NOT NULL,
  `location` varchar(255) NOT NULL,
  `started_at` datetime DEFAULT NULL,
  `ended_at` datetime DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `slug` (`slug`),
  KEY `started_at` (`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Sponsor Table
DROP TABLE IF EXISTS `sponsor`;
CREATE TABLE `sponsor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `description` text NOT NULL,
  `sector` varchar(150) NOT NULL,
  `location` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Join Table: Event - Sponsor
DROP TABLE IF EXISTS `event_sponsor`;
CREATE TABLE `event_sponsor` (
  `event_id` int(11) NOT NULL,
  `sponsor_id` int(11) NOT NULL,
  PRIMARY KEY (`event_id`,`sponsor_id`),
  KEY `fk_sponsor_id` (`sponsor_id`),
  CONSTRAINT `fk_event_id` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sponsor_id` FOREIGN KEY (`sponsor_id`) REFERENCES `sponsor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Join Table: Event - Participation
DROP TABLE IF EXISTS `event_participation`;
CREATE TABLE `event_participation` (
  `event_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`event_id`,`user_id`),
  KEY `fk_part_user` (`user_id`),
  CONSTRAINT `fk_part_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_part_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data Injection
INSERT INTO `user` (`username`, `email`, `password`, `role`) VALUES
('admin', 'admin@ecospot.tn', 'admin123', 'ADMIN'),
('dhia', 'dhia@gmail.com', 'password123', 'USER');

INSERT INTO `category` (`name`) VALUES
('Environment'),
('Workshops'),
('Community');

INSERT INTO `blog` (`title`, `content`, `author`, `image`, `category_id`, `views`) VALUES
('Saving the Ocean', 'Litter in our oceans is a growing problem...', 'dhia', 'ocean.jpg', 1, 150),
('Future of Solar Energy', 'Solar panels are becoming more efficient every day...', 'admin', 'solar.png', 3, 230);

INSERT INTO `event` (`name`, `slug`, `description`, `capacity`, `location`, `started_at`, `ended_at`, `image`, `latitude`, `longitude`) VALUES
('Eco-Green Summit 2026', 'eco-green-summit-2026', 'A summit discussing the latest in green technology.', 500, 'Palais des Congres, Tunis', '2026-06-15 09:00:00', '2026-06-17 18:00:00', 'summit.jpg', 36.8065, 10.1815),
('Beach Cleanup Day', 'beach-cleanup-day', 'Volunteers gathering to clean La Marsa beach.', 100, 'La Marsa, Tunis', '2026-05-20 08:30:00', '2026-05-20 12:00:00', 'cleanup.png', 36.8837, 10.3323);

INSERT INTO `sponsor` (`name`, `description`, `sector`, `location`) VALUES
('EcoTech Solutions', 'Innovative solutions for waste management.', 'Technology', 'Ghazela Technopark'),
('GreenFuture NGO', 'International organization for environmental protection.', 'Non-Profit', 'Geneva, Switzerland');

INSERT INTO `event_sponsor` (`event_id`, `sponsor_id`) VALUES
(1, 1),
(1, 2),
(2, 2);

INSERT INTO `event_participation` (`event_id`, `user_id`) VALUES
(1, 2);

-- 9. Article View Event Table
DROP TABLE IF EXISTS `article_view_event`;
CREATE TABLE `article_view_event` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `article_id` int(11) NOT NULL,
    `user_id` int(11) DEFAULT NULL,
    `session_id` varchar(64) DEFAULT NULL,
    `viewed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_viewed` (`article_id`,`viewed_at`),
    KEY `idx_viewed_at` (`viewed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Article Reaction Event Table
DROP TABLE IF EXISTS `article_reaction_event`;
CREATE TABLE `article_reaction_event` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `article_id` int(11) NOT NULL,
    `user_id` int(11) DEFAULT NULL,
    `reaction` enum('LIKE','DISLIKE') NOT NULL,
    `acted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_reacted` (`article_id`,`acted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Search Term Log Table
DROP TABLE IF EXISTS `search_term_log`;
CREATE TABLE `search_term_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `term` varchar(255) NOT NULL,
    `result_count` int(11) NOT NULL DEFAULT 0,
    `searched_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_searched_at` (`searched_at`),
    KEY `idx_term` (`term`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Article Stats Daily Table
DROP TABLE IF EXISTS `article_stats_daily`;
CREATE TABLE `article_stats_daily` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `article_id` int(11) NOT NULL,
    `stat_date` date NOT NULL,
    `views` int(11) NOT NULL DEFAULT 0,
    `likes` int(11) NOT NULL DEFAULT 0,
    `dislikes` int(11) NOT NULL DEFAULT 0,
    `comments` int(11) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_article_date` (`article_id`,`stat_date`),
    KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

COMMIT;
