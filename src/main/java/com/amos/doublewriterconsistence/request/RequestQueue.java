package com.amos.doublewriterconsistence.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: RequestQueue
 * @Package: com.amos.common.request
 * @author: amos
 * @Description: 请求的队列
 * <p/>
 * 1. 这里需要使用单例模式来确保请求的队列的对象只有一个
 * @date: 2019/7/15 0015 下午 14:18
 * @Version: V1.0
 */
public class RequestQueue {
    /**
     * 构造器私有化，这样就不能通过new来创建实例对象
     * 这里构造器私有化 这点跟枚举一样的，所以我们也可以通过枚举来实现单例模式，详见以后的博文
     */
    private RequestQueue() {
    }

    /**
     * 内存队列
     */
    private List<ArrayBlockingQueue<Request>> queues = new ArrayList<ArrayBlockingQueue<Request>>();
    /**
     * 标志位
     * 主要用来判断读请求去重的操作
     * Boolean true 表示更新数据的操作 false 表示读取缓存的操作
     */
    private Map<String, Boolean> tagMap = new ConcurrentHashMap<>(1);

    /**
     * 私有的静态内部类来实现单例
     */
    private static class Singleton {
        private static RequestQueue queue;

        static {
            queue = new RequestQueue();
        }

        private static RequestQueue getInstance() {
            return queue;
        }
    }

    /**
     * 获取 RequestQueue 对象
     *
     * @return
     */
    public static RequestQueue getInstance() {
        return Singleton.getInstance();
    }

    /**
     * 向容器中添加队列
     *
     * @param queue
     */
    public void add(ArrayBlockingQueue<Request> queue) {
        this.queues.add(queue);
    }

    /**
     * 获取指定的缓存队列
     *
     * @param index
     * @return
     */
    public ArrayBlockingQueue<Request> getQueue(int index) {
        return this.queues.get(index);
    }

    /**
     * 获取队列大小
     *
     * @return
     */
    public int size() {
        return this.queues.size();
    }

    /**
     * 返回标志位
     *
     * @return
     */
    public Map<String, Boolean> getTagMap() {
        return this.tagMap;
    }
}
