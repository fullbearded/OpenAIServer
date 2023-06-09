package com.opaigc.server.application.user.domain;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 描述
 *
 * @author runner.dada@gmail.com
 * @date 2023/4/6
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "member", autoResultMap = true)
public class Member {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户id
     **/
    private Long userId;
    /**
     * 会员到期日
     **/
    private LocalDateTime expireDate;
    /**
     * 每日限额
     **/
    private Long dailyLimit;
    /**
     * 已使用额度-免费
     **/
    private Long freeUsedQuota;
    /**
     * 已使用额度
     **/
    private Long usedQuota;
    /**
     * 总查询额度
     **/
    private Long totalQuota;

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject equities;

    /**
     * 乐观锁版本号
     **/
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 创建人
     **/
    private String createdBy;
    /**
     * 修改时间
     */
    private LocalDateTime updatedAt;
    /**
     * 更新人
     **/
    private String updatedBy;

    public Boolean isExpired() {
        return expireDate.isBefore(LocalDateTime.now());
    }

    public Boolean isFreeUser() {
        return totalQuota == 0 || isExpired();
    }

    public Boolean isVip() {
        return !isFreeUser();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Equities {
        private Boolean gpt4;
    }

}
