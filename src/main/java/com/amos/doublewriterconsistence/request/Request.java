package com.amos.doublewriterconsistence.request;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Package com.amos.common.request
 * @ClassName Request
 * @Description 请求的接口
 * 该接口封装了请求的方法，实现类来实现具体的业务逻辑
 * @Author Amos
 * @Modifier
 * @Date 2019/7/14 17:34
 * @Version 1.0
 **/
public interface Request {
    /**
     * 处理业务
     */
    void process();

    /**
     * 返回库存的id，作为缓存与数据库操作之间的纽带
     *
     * @return
     */
    String getInventoryId();

    /**
     * 是否强制更新
     * 该值主要为了读请求去重准备的，具体的业务逻辑可以详见队列处理时的注释
     *
     * @return
     */
    Boolean isForceRefresh();
}
