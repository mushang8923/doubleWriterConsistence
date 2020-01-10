package com.amos.doublewriterconsistence.type;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: ResultEnum
 * @Package: com.amos.common.type
 * @author: amos
 * @Description:
 * @date: 2019/6/28 0028 上午 9:45
 * @Version: V1.0
 */
public enum ResultEnum {
    SUCCESS(1, "ok"),
    FAIL(0, "校验错误"),
    ERROR(-1, "系统错误");
    private Integer code;
    private String desc;

    private ResultEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static Integer success() {
        return SUCCESS.getCode();
    }

    public static Integer fail() {
        return FAIL.getCode();
    }

    public static Integer error() {
        return ERROR.getCode();
    }
}
