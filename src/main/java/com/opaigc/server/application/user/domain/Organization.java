package com.opaigc.server.application.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opaigc.server.infrastructure.enums.EntityStatusEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author: Runner.dada
 * @date: 2023/5/12
 * @description: 组织表
 **/
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "organization", autoResultMap = true)
public class Organization implements Serializable {

    /**
     * 自增主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户Code
     **/
    private String name;

    /**
     * 状态
     **/
    private EntityStatusEnum status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    private String createdBy;

    /**
     * 修改时间
     */
    private LocalDateTime updatedAt;

    private String updatedBy;
}
