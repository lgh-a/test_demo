SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `zaixianwan` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `zaixianwan`;

DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `user_favorite`;
DROP TABLE IF EXISTS `user_history`;
DROP TABLE IF EXISTS `sys_rate_limit_rule`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `game_info`;
DROP TABLE IF EXISTS `game_series`;
DROP TABLE IF EXISTS `game_console`;

CREATE TABLE `game_console` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT 'Console short name',
  `full_name` varchar(100) NOT NULL COMMENT 'Console full name',
  `icon_url` varchar(255) DEFAULT NULL COMMENT 'Icon URL',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Game consoles';

CREATE TABLE `game_series` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT 'Series name',
  `icon_url` varchar(255) DEFAULT NULL COMMENT 'Icon URL',
  `description` text COMMENT 'Series description',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Game series';

CREATE TABLE `game_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT 'Game title',
  `description` text COMMENT 'Game description',
  `rom_url` varchar(500) NOT NULL COMMENT 'ROM path',
  `thumb_url` varchar(500) DEFAULT NULL COMMENT 'Thumbnail path',
  `console_id` int NOT NULL COMMENT 'Console id',
  `series_id` int DEFAULT NULL COMMENT 'Series id',
  `language` varchar(50) DEFAULT 'Unknown' COMMENT 'Language',
  `region` varchar(50) DEFAULT 'Unknown' COMMENT 'Region',
  `genre` varchar(50) DEFAULT NULL COMMENT 'Genre',
  `status` tinyint DEFAULT 1 COMMENT '1 published, 0 unpublished',
  `is_recommend` tinyint DEFAULT 0 COMMENT '1 recommended, 0 normal',
  `sort` int DEFAULT 0 COMMENT 'Sort order, smaller first',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_game_info_console_id` (`console_id`),
  KEY `idx_game_info_series_id` (`series_id`),
  KEY `idx_game_info_status` (`status`),
  KEY `idx_game_info_recommend_sort` (`is_recommend`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Games';

CREATE TABLE `user_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT 'User id',
  `game_id` int NOT NULL COMMENT 'Game id',
  `last_played_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_history_user_game` (`user_id`, `game_id`),
  KEY `idx_user_history_user_id` (`user_id`),
  KEY `idx_user_history_game_id` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User play history';

CREATE TABLE `user_favorite` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT 'User id',
  `game_id` int NOT NULL COMMENT 'Game id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_favorite_game` (`user_id`, `game_id`),
  KEY `idx_user_favorite_user_id` (`user_id`),
  KEY `idx_user_favorite_game_id` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User favorites';

CREATE TABLE `sys_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL COMMENT 'Password',
  `status` tinyint DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System users';

CREATE TABLE `sys_role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT 'Role name',
  `remark` varchar(255) DEFAULT NULL COMMENT 'Role remark',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System roles';

CREATE TABLE `sys_menu` (
  `id` int NOT NULL AUTO_INCREMENT,
  `parent_id` int DEFAULT 0 COMMENT 'Parent id',
  `name` varchar(50) NOT NULL COMMENT 'Menu name',
  `perms` varchar(100) DEFAULT NULL COMMENT 'Permission key',
  `type` tinyint DEFAULT 1 COMMENT '1 directory, 2 menu, 3 button',
  `sort` int DEFAULT 0 COMMENT 'Sort order',
  `icon` varchar(50) DEFAULT NULL COMMENT 'Icon',
  `path` varchar(200) DEFAULT NULL COMMENT 'Route path',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Menus and permissions';

CREATE TABLE `sys_user_role` (
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User role mapping';

CREATE TABLE `sys_role_menu` (
  `role_id` int NOT NULL,
  `menu_id` int NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role menu mapping';

CREATE TABLE `sys_rate_limit_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `config_key` varchar(100) NOT NULL COMMENT 'Rule config key',
  `name` varchar(100) DEFAULT NULL COMMENT 'Rule name',
  `description` varchar(500) DEFAULT NULL COMMENT 'Rule description',
  `limit_type` varchar(20) NOT NULL DEFAULT 'TOKEN_BUCKET' COMMENT 'TOKEN_BUCKET or FLEXIBLE',
  `capacity` int NOT NULL DEFAULT 100 COMMENT 'Logged-in user capacity',
  `rate` int NOT NULL DEFAULT 10 COMMENT 'Logged-in user refill rate',
  `guest_capacity` int DEFAULT NULL COMMENT 'Guest capacity',
  `guest_rate` int DEFAULT NULL COMMENT 'Guest refill rate',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` int NOT NULL DEFAULT 1 COMMENT 'Rule version',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_rate_limit_rule_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Dynamic rate limit rules';

INSERT INTO `game_console` (`id`, `name`, `full_name`, `icon_url`) VALUES
(1, 'FC', 'Family Computer', NULL),
(2, 'GBA', 'Game Boy Advance', NULL),
(3, 'SFC', 'Super Famicom', NULL);

INSERT INTO `game_series` (`id`, `name`, `icon_url`, `description`) VALUES
(1, 'Mario', NULL, 'Mario series'),
(2, 'Pokemon', NULL, 'Pokemon series'),
(3, 'Test', NULL, 'Local playable test data');

INSERT INTO `game_info` (`id`, `name`, `description`, `rom_url`, `thumb_url`, `console_id`, `series_id`, `language`, `region`, `genre`) VALUES
(1, 'Super Mario Bros', 'Classic FC game', '/roms/mario.nes', '/thumbs/mario.jpg', 1, 1, 'Chinese', 'US', NULL),
(2, 'Pokemon Emerald', 'Classic GBA RPG', '/roms/pokemon_emerald.gba', '/thumbs/pokemon.jpg', 2, 2, 'Chinese', 'JP', NULL),
(3, 'Test ROM', 'Playable local test ROM', '/roms/Adventure Island 4 (English v1.0).nes', '/thumbs/t1.png', 1, 3, 'Chinese', 'US', NULL);

INSERT INTO `sys_user` (`id`, `username`, `password`, `status`) VALUES
(1, 'admin', '123456', 1),
(2, 'player', '123456', 1);

INSERT INTO `sys_role` (`id`, `name`, `remark`) VALUES
(1, 'admin', 'Super Administrator'),
(2, 'user', 'Normal User');

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `perms`, `type`, `sort`, `icon`, `path`) VALUES
(1, 0, 'System Management', '', 1, 1, 'settings', '/admin'),
(2, 1, 'Role Management', 'sys:role:list', 2, 1, 'role', NULL),
(3, 1, 'Menu Management', 'sys:menu:list', 2, 2, 'menu', NULL),
(4, 0, 'Game Module', '', 1, 2, 'game', '/games'),
(5, 4, 'Game List', 'game:list', 2, 1, 'list', '/games'),
(6, 4, 'Play Game', 'game:play', 3, 2, NULL, NULL),
(7, 2, 'Add Role', 'sys:role:add', 3, 1, NULL, NULL),
(8, 2, 'Delete Role', 'sys:role:delete', 3, 2, NULL, NULL),
(9, 3, 'Add Menu', 'sys:menu:add', 3, 1, NULL, NULL),
(10, 3, 'Update Menu', 'sys:menu:update', 3, 2, NULL, NULL),
(11, 3, 'Delete Menu', 'sys:menu:delete', 3, 3, NULL, NULL),
(12, 1, 'Rate Limit Rules', 'rate-limit:rule:list', 2, 3, 'limit', NULL),
(13, 12, 'View Rule', 'rate-limit:rule:view', 3, 1, NULL, NULL),
(14, 12, 'Add Rule', 'rate-limit:rule:add', 3, 2, NULL, NULL),
(15, 12, 'Edit Rule', 'rate-limit:rule:edit', 3, 3, NULL, NULL),
(16, 12, 'Delete Rule', 'rate-limit:rule:delete', 3, 4, NULL, NULL),
(17, 12, 'Refresh Cache', 'rate-limit:rule:refresh', 3, 5, NULL, NULL),
(18, 1, 'User Management', 'sys:user:list', 2, 0, 'user', NULL),
(19, 18, 'Add User', 'sys:user:add', 3, 1, NULL, NULL),
(20, 18, 'Update User', 'sys:user:update', 3, 2, NULL, NULL),
(21, 18, 'Reset Password', 'sys:user:reset-password', 3, 3, NULL, NULL),
(22, 1, 'Game Management', 'game:manage:list', 2, 4, 'game', '/admin/games'),
(23, 22, 'Add Game', 'game:manage:add', 3, 1, NULL, NULL),
(24, 22, 'Update Game', 'game:manage:update', 3, 2, NULL, NULL),
(25, 22, 'Delete Game', 'game:manage:delete', 3, 3, NULL, NULL),
(26, 1, 'Console Management', 'console:manage:list', 2, 5, 'console', '/admin/consoles'),
(27, 26, 'Add Console', 'console:manage:add', 3, 1, NULL, NULL),
(28, 26, 'Update Console', 'console:manage:update', 3, 2, NULL, NULL),
(29, 26, 'Delete Console', 'console:manage:delete', 3, 3, NULL, NULL),
(30, 1, 'Series Management', 'series:manage:list', 2, 6, 'series', '/admin/series'),
(31, 30, 'Add Series', 'series:manage:add', 3, 1, NULL, NULL),
(32, 30, 'Update Series', 'series:manage:update', 3, 2, NULL, NULL),
(33, 30, 'Delete Series', 'series:manage:delete', 3, 3, NULL, NULL);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(1, 10),
(1, 11),
(1, 12),
(1, 13),
(1, 14),
(1, 15),
(1, 16),
(1, 17),
(1, 18),
(1, 19),
(1, 20),
(1, 21),
(1, 22),
(1, 23),
(1, 24),
(1, 25),
(1, 26),
(1, 27),
(1, 28),
(1, 29),
(1, 30),
(1, 31),
(1, 32),
(1, 33),
(2, 4),
(2, 5),
(2, 6);

SET FOREIGN_KEY_CHECKS = 1;
