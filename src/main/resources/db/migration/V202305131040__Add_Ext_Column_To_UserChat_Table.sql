-- ----------------------------
-- Table structure for app
-- ----------------------------
ALTER TABLE `user_chat` ADD COLUMN `ext` json  COMMENT '扩展字段' AFTER `answers`;