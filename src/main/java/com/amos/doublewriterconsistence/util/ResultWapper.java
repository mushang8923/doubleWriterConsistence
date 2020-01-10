package com.amos.doublewriterconsistence.util;

import com.amos.doublewriterconsistence.bean.Result;
import com.amos.doublewriterconsistence.type.ResultEnum;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: ResultWapper
 * @Package: com.amos.common.util
 * @author: amos
 * @Description:
 * @date: 2019/6/28 0028 上午 9:58
 * @Version: V1.0
 */
public class ResultWapper {
    /**
     * 初始化Result
     *
     * @param code
     * @param msg
     * @param data
     * @return
     */
    public static Result result(Integer code, String msg, Object data) {
        return new Result(code, msg, data);
    }

    /**
     * 返回成功（附带数据）
     *
     * @param data
     * @return
     */
    public static Result success(Object data) {
        return result(ResultEnum.success(), ResultEnum.SUCCESS.getDesc(), data);
    }

    /**
     * 返回成功 没有数据
     *
     * @return
     */
    public static Result success() {
        return result(ResultEnum.success(), ResultEnum.SUCCESS.getDesc(), null);
    }

    /**
     * 返回校验失败
     *
     * @param msg
     * @return
     */
    public static Result fail(String msg) {
        return result(ResultEnum.fail(), ResultEnum.FAIL.getDesc(), null);
    }

    /**
     * 返回失败信息
     *
     * @param msg
     * @return
     */
    public static Result error(String msg) {
        return result(ResultEnum.error(), ResultEnum.ERROR.getDesc(), null);
    }
}
