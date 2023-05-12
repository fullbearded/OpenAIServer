-- ----------------------------
-- Table structure for member
-- ----------------------------
ALTER TABLE `users` ADD COLUMN `organization_id` json  COMMENT '组织ID' AFTER `code`;