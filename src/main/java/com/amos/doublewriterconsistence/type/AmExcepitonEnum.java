package com.amos.doublewriterconsistence.type;

import com.amos.doublewriterconsistence.exception.RabbitMQExceptionAssert;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: AmExcepitonEnum
 * @Package: com.amos.common.type
 * @author: amos
 * @Description:
 * @date: 2019/7/10 0010 下午 17:12
 * @Version: V1.0
 */
public enum AmExcepitonEnum implements RabbitMQExceptionAssert {
    NOT_NULL(0, "参数不能为空");
    private int code;
    private String msg;

    private AmExcepitonEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
