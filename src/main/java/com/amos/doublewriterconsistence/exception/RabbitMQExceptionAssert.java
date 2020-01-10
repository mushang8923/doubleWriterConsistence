package com.amos.doublewriterconsistence.exception;

import java.text.MessageFormat;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: RabbitMQExceptionAssert
 * @Package: com.amos.common.exception
 * @author: amos
 * @Description:
 * @date: 2019/7/10 0010 下午 14:52
 * @Version: V1.0
 */
public interface RabbitMQExceptionAssert extends IResult, Assert {

    @Override
    default BaseException newException(Object... args) {
        String msg = MessageFormat.format(this.getMsg(), args);

        return new RabbitMQException(this, args, msg);
    }

    @Override
    default BaseException newException(Throwable t, Object... args) {
        String msg = MessageFormat.format(this.getMsg(), args);

        return new RabbitMQException(this, args, msg, t);
    }
}
