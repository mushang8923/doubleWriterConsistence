package com.amos.doublewriterconsistence.service.impl;

import com.amos.doublewriterconsistence.request.Request;
import com.amos.doublewriterconsistence.request.RequestQueue;
import com.amos.doublewriterconsistence.service.RequestAsyncProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: double-writer-consistence
 * @ClassName: RequestAsyncProcessServiceImpl
 * @Package: com.amos.doublewriterconsistence.service.impl
 * @author: amos
 * @Description:
 * @date: 2019/8/19 0019 下午 15:23
 * @Version: V1.0
 */
@Service
public class RequestAsyncProcessServiceImpl implements RequestAsyncProcessService {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 路由到指定的缓存队列中
     * doubleWriterConsistence
     *
     * @param request
     */
    @Override
    public void route(Request request) {
        try {
            // 做请求的路由，根据每个请求的商品id，路由到对应的内存队列中去
            ArrayBlockingQueue<Request> queue = this.getRoutingQueue(request.getInventoryId());
            // 将请求放入对应的队列中，完成路由操作
            queue.put(request);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据库存记录路由到指定的缓存队列
     *
     * @param key
     * @return
     */
    private ArrayBlockingQueue<Request> getRoutingQueue(String key) {
        RequestQueue requestQueue = RequestQueue.getInstance();
        int h;
        int hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >> 16);
        // 对hash值取模，将hash值路由到指定的内存队列中，比如内存队列大小8
        // 用内存队列的数量对hash值取模之后，结果一定是在0~7之间
        // 所以任何一个商品id都会被固定路由到同样的一个内存队列中去的
        int index = (requestQueue.size() - 1) & hash;
        this.logger.info("路由的缓存队列为：{}", index);
        return requestQueue.getQueue(index);
    }
}
