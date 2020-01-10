package com.amos.doublewriterconsistence.exception;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: BaseException
 * @Package: com.amos.common.exception
 * @author: amos
 * @Description: 自定义封装所有异常的基类
 * <p/>
 * 该异常继承RuntimeException
 * @date: 2019/7/9 0009 上午 8:15
 * @Version: V1.0
 */
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = 1702524362131670363L;
    /**
     * 异常的返回的编码和信息
     */
    protected IResult result;
    /**
     * 异常消息参数
     */
    protected Object[] args;

    public BaseException(IResult result) {
        super(result.getMsg());
        this.result = result;
    }

    public BaseException(int code, String msg) {
        super(msg);
        this.result = new IResult() {
            @Override
            public int getCode() {
                return code;
            }

            @Override
            public String getMsg() {
                return msg;
            }
        };
    }

    public BaseException(IResult result, Object[] args, String msg) {
        super(msg);
        this.result = result;
        this.args = args;
    }

    public BaseException(IResult result, Object[] args, String message, Throwable cause) {
        super(message, cause);
        this.result = result;
        this.args = args;
    }
}
