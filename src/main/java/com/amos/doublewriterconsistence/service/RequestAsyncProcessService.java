package com.amos.doublewriterconsistence.service;

import com.amos.doublewriterconsistence.request.Request;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: double-writer-consistence
 * @ClassName: RequestAsyncProcessService
 * @Package: com.amos.doublewriterconsistence.service
 * @author: amos
 * @Description: 请求异步执行的Service
 * 该接口主要根据库存记录的值来计算将任何库存记录路由到同一个缓存队列中
 * 1. 获取库存记录的hash值
 * 2. 用队列数量对hash值取模，结果一定是在一个区间的（区间是缓存队列的数量）
 * 3. 将这个请求路由到这个缓存队列中
 * @date: 2019/8/19 0019 下午 15:16
 * @Version: V1.0
 */
public interface RequestAsyncProcessService {
    /**
     * 将请求指定到固定的队列上
     *
     * @param request
     */
    void route(Request request);
}
