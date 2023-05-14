-- ----------------------------
-- Table structure for users
-- ----------------------------
ALTER TABLE users MODIFY COLUMN status varchar(20)  DEFAULT 'ENABLED' NULL COMMENT '帐号状态：ENABLED 正常使用 DISABLED 禁用';
