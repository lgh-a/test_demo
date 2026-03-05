-- sys_user
CREATE TABLE `sys_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL COMMENT 'MD5 or BCrypt password',
  `status` tinyint DEFAULT 1 COMMENT '1: Normal, 0: Disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Users';

-- sys_role
CREATE TABLE `sys_role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT 'Role Name, e.g. admin',
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Roles';

-- sys_menu (Permissions / Menus)
CREATE TABLE `sys_menu` (
  `id` int NOT NULL AUTO_INCREMENT,
  `parent_id` int DEFAULT 0 COMMENT 'Parent ID',
  `name` varchar(50) NOT NULL COMMENT 'Menu Name',
  `perms` varchar(100) DEFAULT NULL COMMENT 'Permission String, e.g. game:play',
  `type` tinyint DEFAULT 1 COMMENT '1: Menu, 2: Button',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Menus and Permissions';

-- sys_user_role
CREATE TABLE `sys_user_role` (
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Role Mapping';

-- sys_role_menu
CREATE TABLE `sys_role_menu` (
  `role_id` int NOT NULL,
  `menu_id` int NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role Menu Mapping';

-- Insert Default Data
INSERT INTO `sys_user` (`id`, `username`, `password`) VALUES (1, 'admin', '123456'); -- In a real app, this should be hashed
INSERT INTO `sys_user` (`id`, `username`, `password`) VALUES (2, 'player', '123456');

INSERT INTO `sys_role` (`id`, `name`, `remark`) VALUES (1, 'admin', 'Super Administrator'), (2, 'user', 'Normal User');

-- Menus and buttons
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `perms`, `type`) VALUES 
(1, 0, '系统管理', '', 1),
(2, 1, '角色管理', 'sys:role:list', 1),
(3, 1, '菜单管理', 'sys:menu:list', 1),
(4, 0, '游戏模块', '', 1),
(5, 4, '全部游戏', 'game:list', 1),
(6, 4, '开始游戏 (按钮)', 'game:play', 2),
(7, 2, '添加角色', 'sys:role:add', 2),
(8, 2, '删除角色', 'sys:role:delete', 2),
(9, 3, '添加菜单', 'sys:menu:add', 2);

-- Admin gets all roles (1 -> 1)
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);
-- Player gets user role (2 -> 2)
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (2, 2);

-- Admin role gets all menus
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) SELECT 1, id FROM sys_menu;
-- User role only gets game list and play
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 4), (2, 5), (2, 6);
