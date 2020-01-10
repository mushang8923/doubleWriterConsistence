package com.amos.doublewriterconsistence.thread;

import com.amos.doublewriterconsistence.request.Request;
import com.amos.doublewriterconsistence.request.RequestQueue;
import com.amos.doublewriterconsistence.request.impl.InventoryCacheRequest;
import com.amos.doublewriterconsistence.request.impl.InventoryDBRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: RequestThread
 * @Package: com.amos.common.thread
 * @author: amos
 * @Description: 执行请求的工作线程
 * <p/>
 * 线程和队列进行绑定，然后再线程中处理对应的业务逻辑
 * @date: 2019/7/15 0015 下午 14:34
 * @Version: V1.0
 */
public class RequestThread implements Callable<Boolean> {
    public final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 队列
     */
    private ArrayBlockingQueue<Request> queue;

    public RequestThread(ArrayBlockingQueue<Request> queue) {
        this.queue = queue;
    }

    /**
     * 方法中执行具体的业务逻辑
     *
     * @return
     * @throws Exception
     */
    @Override
    public Boolean call() throws Exception {
        try {
            while (true) {
                // ArrayBlockingQueue take方法 获取队列排在首位的对象，如果队列为空或者队列满了，则会被阻塞住
                Request request = this.queue.take();
                Boolean forceFresh = request.isForceRefresh();
                // 如果需要更新的话
                if (!forceFresh) {
                    RequestQueue requestQueue = RequestQueue.getInstance();
                    Map<String, Boolean> tagMap = requestQueue.getTagMap();
                    // 如果是请求缓存中的数据
                    if (request instanceof InventoryCacheRequest) {
                        Boolean tag = tagMap.get(request.getInventoryId());
                        // 如果tag为空 则说明读取缓存的操作
                        if (null == tag) {
                            tagMap.put(request.getInventoryId(), Boolean.FALSE);
                        }
                        // tag为不为空，并且为true时，说明上一个请求是更新数据库的
                        // 那么此时我们需要将标志位修改为False
                        if (tag != null && tag) {
                            tagMap.put(request.getInventoryId(), Boolean.FALSE);
                        }

                        // tag不为空，并且为false时，说明前面已经有数据库+缓存的请求了，
                        // 那么这个请求应该是读请求，可以直接过滤掉了，不要添加到队列中
                        if (tag != null && !tag) {
                            return Boolean.TRUE;
                        }

                    } else if (request instanceof InventoryDBRequest) {
                        // 如果是更新数据库的操作
                        tagMap.put(request.getInventoryId(), Boolean.TRUE);
                    }
                }
                // 执行请求处理
                this.logger.info("缓存队列执行+++++++++++++++++，{}", request.getInventoryId());
                request.process();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }
}
