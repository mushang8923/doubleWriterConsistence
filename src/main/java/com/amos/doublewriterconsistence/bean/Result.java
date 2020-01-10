package com.amos.doublewriterconsistence.bean;

import lombok.Data;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: Result
 * @Package: com.amos.common.bean
 * @author: amos
 * @Description: 结果类
 * @date: 2019/6/28 0028 上午 9:51
 * @Version: V1.0
 */
@Data
public class Result {
    /**
     * 编码
     */
    private Integer code;
    /**
     * 信息
     */
    private String msg;
    /**
     * 数据
     */
    private Object data;

    public Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result() {
    }

}
