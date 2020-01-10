package com.amos.doublewriterconsistence.util;

import com.amos.doublewriterconsistence.exception.RabbitMQException;
import com.amos.doublewriterconsistence.type.AmExcepitonEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Package com.amos.common.util
 * @ClassName RedisUtils
 * @Description redis的操作类
 * @Author Amos
 * @Modifier
 * @Date 2019/8/18 0:13
 * @Version 1.0
 **/
@Component
public class RedisUtils {
    public final Log logger = LogFactory.getLog(this.getClass());
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 指定键缓存实效的时间
     *
     * @param key        指定的键
     * @param expireTime 超时时间 毫秒
     * @return
     */
    public boolean expire(String key, long expireTime) {
        AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "指定的键不能为空");

        if (expireTime < 0) {
            throw new RabbitMQException("超时时间不能小于0");
        }
        try {
            return this.redisTemplate.expire(key, expireTime, TimeUnit.MICROSECONDS);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            return Boolean.FALSE;
        }
    }

    /**
     * 判断是否有指定的key
     *
     * @param key 指定的键
     * @return
     */
    public boolean hasKey(String key) {
        AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "指定的键不能为空");

        try {
            return this.redisTemplate.hasKey(key);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            return Boolean.FALSE;
        }
    }

    /**
     * 保存键值
     *
     * @param key   保存的键
     * @param value 保存的值
     * @return
     */
    public boolean save(String key, Object value) {
        AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "指定的键不能为空");

        try {
            this.redisTemplate.opsForValue().set(key, value);
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
            if (this.logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            return Boolean.FALSE;
        }
    }

    /**
     * 删除key
     *
     * @param key
     * @return
     */
    public boolean del(String key) {
        AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "指定的键不能为空");
        try {
            return this.redisTemplate.delete(key);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            return Boolean.FALSE;
        }
    }

    /**
     * 保存有实效时间的键值对
     *
     * @param key
     * @param value
     * @param expireTime 实效时间 单位毫秒
     * @return
     */
    public boolean save(String key, Object value, long expireTime) {
        AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "键值不能为空");

        try {
            this.redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.MICROSECONDS);
            return Boolean.TRUE;
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            return Boolean.FALSE;
        }
    }

    /**
     * 获取指定key的值
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "指定的键值不能为空");
        try {
            return this.redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
