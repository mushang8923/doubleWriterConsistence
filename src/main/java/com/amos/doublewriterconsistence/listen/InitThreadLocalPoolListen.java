package com.amos.doublewriterconsistence.listen;

import com.amos.doublewriterconsistence.thread.RequestThreadPool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Package com.amos.common.listener
 * @ClassName InitThreadLocalPoolListen
 * @Description 系统初始化监听器 初始队列
 * @Author Amos
 * @Modifier
 * @Date 2019/7/14 16:44
 * @Version 1.0
 **/
public class InitThreadLocalPoolListen implements ServletContextListener {
    /**
     * 系统初始化队列
     *
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        RequestThreadPool.getInstance();
    }

    /**
     * 监听器销毁执行的逻辑
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
