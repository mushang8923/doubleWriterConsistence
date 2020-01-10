package com.amos.doublewriterconsistence.thread;

import com.amos.doublewriterconsistence.request.Request;
import com.amos.doublewriterconsistence.request.RequestQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Package com.amos.common.thread
 * @ClassName RequestThreadPool
 * @Description 请求线程池
 * 1. 使用线程池来管理线程，该线程池必须是单例的
 * 2. 线程池初始化成功后，创建缓存队列，并且和线程池进行绑定
 * @Author Amos
 * @Modifier
 * @Date 2019/7/14 16:47
 * @Version 1.0
 **/
public class RequestThreadPool {
    /**
     * 核心线程数
     */
    private Integer corePoolSize = 10;
    /**
     * 线程池最大线程数
     */
    private Integer maximumPoolSize = 20;

    /**
     * 线程最大存活时间
     */
    private Long keepAliveTime = 60L;

    /**
     * 初始化线程池 这里我们不使用Executors.newFixedThreadPool()方式，该种方式不推荐使用，
     * 主要是因为默认允许的队列的长度是Integer.MAX_VALUE,可能会造成OOM
     * 第一个参数：corePoolSize: 线程中核心线程数的最大值（能同时运行的最大的线程数）
     * 第二个参数：maximumPoolSize: 线程池中线程数的最大值
     * 第三个参数：keepAliveTime: 线程存活时间
     * 第四个参数：unit：时间单位
     * 第五个参数：BlockingQueue: 用务于缓存任的队列 这里使用 ArrayBlockingQueue 这个是有界队列
     */
    private ExecutorService threadPool = new ThreadPoolExecutor(this.corePoolSize, this.maximumPoolSize,
            this.keepAliveTime, TimeUnit.SECONDS,
            new ArrayBlockingQueue(this.corePoolSize));


    /**
     * 构造器私有化，这样就不能通过new来创建实例对象
     * <p>
     * 类实例化的时候 ，初始化队列的大小，并且绑定队列和线程池以及队列与线程的关系
     * <p>
     * 初始化指定数量的队列
     */
    public RequestThreadPool() {
        /**
         *缓存队列集合来管理所有的缓存队列
         */
        RequestQueue requestQueue = RequestQueue.getInstance();
        for (int i = 0; i < this.corePoolSize; i++) {
            /**
             * 缓存队列使用Request 接口来作为泛型，将可以将队列的类型添加定义，同时也可以通过多态的特性来实现子类的扩展
             * 目前Request只是定义，业务可以之后实现
             */
            ArrayBlockingQueue<Request> queue = new ArrayBlockingQueue<>(this.corePoolSize);
            requestQueue.add(queue);
            // 线程池和缓存队列通过线程来绑定
            // 每个线程对应一个队列
            this.threadPool.submit(new RequestThread(queue));
        }
    }

    /**
     * 使用静态内部类来实现单例的模式（绝对的线程安全）
     */
    private static class Singleton {
        /**
         * 私有的静态变量，确保该变量不会被外部调用
         */
        private static RequestThreadPool requestThreadPool;

        /**
         * 静态代码块在类初始化时执行一次
         */
        static {
            requestThreadPool = new RequestThreadPool();
        }

        /**
         * 静态内部类对外提供实例的获取方法
         *
         * @return
         */
        public static RequestThreadPool getInstance() {
            return requestThreadPool;
        }
    }

    /**
     * 请求线程池类对外提供获取实例的方法 由于外部类没有RequestThreadPool的实例对象，所以除了该方法，外部类无法创建额外的RequestThreadPool对象
     *
     * @return
     */
    public static RequestThreadPool getInstance() {
        return Singleton.getInstance();
    }


}
