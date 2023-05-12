-- ----------------------------
-- Table structure for organization
-- ----------------------------
CREATE TABLE `organization`
(
    `id`         BIGINT UNSIGNED PRIMARY KEY COMMENT 'ID',
    `name`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '组织名称',
    `status`     VARCHAR(10)  NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED 启用， DISABLED 停用',
    `created_at` DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `created_by` VARCHAR(64)           DEFAULT '' COMMENT '创建者',
    `updated_at` DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    `updated_by` VARCHAR(64)           DEFAULT '' COMMENT '更新者'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='组织表';