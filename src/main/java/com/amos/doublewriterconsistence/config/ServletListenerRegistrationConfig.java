package com.amos.doublewriterconsistence.config;

import com.amos.doublewriterconsistence.listen.InitThreadLocalPoolListen;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Package com.amos.common.config
 * @ClassName ServletListenerRegistrationConfig
 * @Description 在容器启动的时候，注册自定义的Listener
 * 1. 在监听器中初始化线程池
 * @Author Amos
 * @Modifier
 * @Date 2019/7/14 16:41
 * @Version 1.0
 **/
@Configuration
public class ServletListenerRegistrationConfig {

    /**
     * 注册自定义的Bean
     * 并且设置监听器，该监听器初始化线程池
     *
     * @return
     */
    @Bean
    public ServletListenerRegistrationBean registrationBean() {
        ServletListenerRegistrationBean servletListenerRegistrationBean = new ServletListenerRegistrationBean();
        servletListenerRegistrationBean.setListener(new InitThreadLocalPoolListen());
        return servletListenerRegistrationBean;
    }
}
