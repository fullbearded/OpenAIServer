-- ----------------------------
-- Table structure for member
-- ----------------------------
ALTER TABLE `member` ADD COLUMN `equities` json  COMMENT '会员权益' AFTER `total_quota`;