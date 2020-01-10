package com.amos.doublewriterconsistence.exception;


import com.amos.doublewriterconsistence.type.ResultEnum;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: RabbitMQException
 * @Package: com.amos.producer
 * @author: amos
 * @Description:
 * @date: 2019/6/28 0028 上午 9:23
 * @Version: V1.0
 */
public class RabbitMQException extends BaseException {
    private static final long serialVersionUID = 284587800137211341L;

    private int code;
    private String msg;

    public RabbitMQException(int code, String msg) {
        super(code, msg);
    }

    public RabbitMQException(String msg) {
        super(ResultEnum.error(), msg);
    }

    public RabbitMQException(IResult result) {
        super(result);
    }

    public RabbitMQException(IResult result, Object[] args, String message) {
        super(result, args, message);
    }

    public RabbitMQException(IResult result, Object[] args, String message, Throwable cause) {
        super(result, args, message, cause);
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
