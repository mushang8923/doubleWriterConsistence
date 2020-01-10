package com.amos.doublewriterconsistence.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Package com.amos.common.config
 * @ClassName RedisConfig
 * @Description SpringBoot集成Redis的配置类
 * <p>
 * 主要是用来创建RedisTemplate的实例对象
 * @Author Amos
 * @Modifier
 * @Date 2019/8/18 0:03
 * @Version 1.0
 **/
@Configuration
public class RedisConfig {
    /**
     * 系统初始化一个String,Object的redisTemplate对象，便于操作数据
     * 使用fastjson来序列化
     *
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        /**
         * 使用fastJson 来序列化
         */
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(Object.class);
        // value值使用fastjson序列化
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        // hash值使用fastjson序列化
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
        // key的值使用StringRedisSerializer来序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return redisTemplate;

    }
}
