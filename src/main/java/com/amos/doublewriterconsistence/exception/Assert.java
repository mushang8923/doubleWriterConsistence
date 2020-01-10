package com.amos.doublewriterconsistence.exception;

import org.springframework.util.StringUtils;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: Assert
 * @Package: com.amos.common.exception
 * @author: amos
 * @Description: 自定义Assert
 * <p/>
 * 该接口封装了异常的基类，并且提供default方法来抛出异常
 * @date: 2019/7/9 0009 上午 8:14
 * @Version: V1.0
 */
public interface Assert {
    /**
     * 创建异常
     *
     * @param args 异常的信息
     * @return
     */
    BaseException newException(Object... args);

    /**
     * 创建异常
     *
     * @param throwable 所有异常的父类
     * @param args      异常信息
     * @return
     */
    BaseException newException(Throwable throwable, Object... args);

    /**
     * 判断对象是否为空，如果为空的话就抛出异常
     *
     * @param obj 待判断的对象
     */
    default void assertNotEmpty(Object obj) {
        if (StringUtils.isEmpty(obj)) {
            throw this.newException(obj);
        }
    }

    /**
     * 判断对象是否为空,如果为空就抛出异常
     *
     * @param object 待判断的对象
     * @param args   异常的信息
     */
    default void assertNotEmpty(Object object, Object... args) {
        if (StringUtils.isEmpty(object)) {
            throw this.newException(args);
        }
    }
}
