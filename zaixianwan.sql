/*
 Navicat Premium Data Transfer

 Source Server         : centos7 mysql8 123456
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : 192.168.142.33:3306
 Source Schema         : zaixianwan

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 05/03/2026 16:10:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for game_console
-- ----------------------------
DROP TABLE IF EXISTS `game_console`;
CREATE TABLE `game_console`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Console short name, e.g. FC',
  `full_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Console full name, e.g. 红白机',
  `icon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Icon URL',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Game Consoles' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_console
-- ----------------------------
INSERT INTO `game_console` VALUES (1, 'FC', '红白机', NULL);
INSERT INTO `game_console` VALUES (2, 'GBA', 'Game Boy Advance', NULL);
INSERT INTO `game_console` VALUES (3, 'SFC', '超级任天堂', NULL);

-- ----------------------------
-- Table structure for game_info
-- ----------------------------
DROP TABLE IF EXISTS `game_info`;
CREATE TABLE `game_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Game Title',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Game background and controls info',
  `rom_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Path to the ROM file',
  `thumb_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Path to the thumbnail image',
  `console_id` int NOT NULL COMMENT 'FK to game_console',
  `series_id` int NULL DEFAULT NULL COMMENT 'FK to game_series',
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'Unknown' COMMENT 'Language, e.g. 中文, English',
  `region` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'Unknown' COMMENT 'Region, e.g. JP, US, EU',
  `genre` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Genre, e.g. ACT, RPG',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_console_id`(`console_id` ASC) USING BTREE,
  INDEX `idx_series_id`(`series_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Games' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_info
-- ----------------------------
INSERT INTO `game_info` VALUES (1, '超级马里奥兄弟', '经典FC游戏', '/roms/mario.nes', '/thumbs/mario.jpg', 1, 1, '中文', 'US', NULL, '2026-03-05 10:13:24', '2026-03-05 10:13:24');
INSERT INTO `game_info` VALUES (2, '口袋妖怪 绿宝石', 'GBA经典RPG', '/roms/pokemon_emerald.gba', '/thumbs/pokemon.jpg', 2, 2, '中文', 'JP', NULL, '2026-03-05 10:13:24', '2026-03-05 10:13:24');
INSERT INTO `game_info` VALUES (3, '测试rom', '测试rom', '/roms/Adventure Island 4 (English v1.0).nes', '/thumbs/t1.png', 1, 3, '中文', 'US', NULL, '2026-03-05 14:33:59', '2026-03-05 15:34:32');

-- ----------------------------
-- Table structure for game_series
-- ----------------------------
DROP TABLE IF EXISTS `game_series`;
CREATE TABLE `game_series`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Series name, e.g. Mario',
  `icon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Icon URL',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Series description',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Game Series' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_series
-- ----------------------------
INSERT INTO `game_series` VALUES (1, '马里奥', NULL, 'Mario Series');
INSERT INTO `game_series` VALUES (2, '口袋妖怪', NULL, 'Pokemon Series');
INSERT INTO `game_series` VALUES (3, '测试', NULL, '测试');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `parent_id` int NULL DEFAULT 0 COMMENT 'Parent ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Menu Name',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Permission String, e.g. game:play',
  `type` tinyint NULL DEFAULT 1 COMMENT '1: Menu, 2: Button',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Menus and Permissions' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '系统管理', '', 1);
INSERT INTO `sys_menu` VALUES (2, 1, '角色管理', 'sys:role:list', 1);
INSERT INTO `sys_menu` VALUES (3, 1, '菜单管理', 'sys:menu:list', 1);
INSERT INTO `sys_menu` VALUES (4, 0, '游戏模块', '', 1);
INSERT INTO `sys_menu` VALUES (5, 4, '全部游戏', 'game:list', 1);
INSERT INTO `sys_menu` VALUES (6, 4, '开始游戏 (按钮)', 'game:play', 2);
INSERT INTO `sys_menu` VALUES (7, 2, '添加角色', 'sys:role:add', 2);
INSERT INTO `sys_menu` VALUES (8, 2, '删除角色', 'sys:role:delete', 2);
INSERT INTO `sys_menu` VALUES (9, 3, '添加菜单', 'sys:menu:add', 2);
INSERT INTO `sys_menu` VALUES (10, 3, '修改菜单', 'sys:menu:update', 3);
INSERT INTO `sys_menu` VALUES (11, 3, '删除菜单', 'sys:menu:delete', 3);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Role Name, e.g. admin',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Roles' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'admin', 'Super Administrator');
INSERT INTO `sys_role` VALUES (2, 'user', 'Normal User');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` int NOT NULL,
  `menu_id` int NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Role Menu Mapping' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1);
INSERT INTO `sys_role_menu` VALUES (1, 2);
INSERT INTO `sys_role_menu` VALUES (1, 3);
INSERT INTO `sys_role_menu` VALUES (1, 4);
INSERT INTO `sys_role_menu` VALUES (1, 5);
INSERT INTO `sys_role_menu` VALUES (1, 6);
INSERT INTO `sys_role_menu` VALUES (1, 7);
INSERT INTO `sys_role_menu` VALUES (1, 8);
INSERT INTO `sys_role_menu` VALUES (1, 9);
INSERT INTO `sys_role_menu` VALUES (1, 10);
INSERT INTO `sys_role_menu` VALUES (1, 11);
INSERT INTO `sys_role_menu` VALUES (2, 4);
INSERT INTO `sys_role_menu` VALUES (2, 5);
INSERT INTO `sys_role_menu` VALUES (2, 6);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MD5 or BCrypt password',
  `status` tinyint NULL DEFAULT 1 COMMENT '1: Normal, 0: Disabled',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Users' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '123456', 1, '2026-03-05 10:42:57');
INSERT INTO `sys_user` VALUES (2, 'player', '123456', 1, '2026-03-05 10:42:57');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User Role Mapping' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);

-- ----------------------------
-- Table structure for user_history
-- ----------------------------
DROP TABLE IF EXISTS `user_history`;
CREATE TABLE `user_history`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT 'User ID (mock for now)',
  `game_id` int NOT NULL COMMENT 'FK to game_info',
  `last_played_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_game`(`user_id` ASC, `game_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User History' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_history
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
