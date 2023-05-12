package com.opaigc.server.infrastructure.enums;

/**
 * 描述
 *
 * @author huhongda@fiture.com
 * @date 2023/5/12
 */

public enum EntityStatusEnum {
    ENABLED("正常"), DISABLED("禁用");

    private String desc;

    EntityStatusEnum(String desc) {
        this.desc = desc;
    }
}
