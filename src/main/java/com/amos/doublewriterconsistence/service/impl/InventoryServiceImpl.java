package com.amos.doublewriterconsistence.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.amos.doublewriterconsistence.entity.Inventory;
import com.amos.doublewriterconsistence.mapper.InventoryMapper;
import com.amos.doublewriterconsistence.service.InventoryService;
import com.amos.doublewriterconsistence.type.AmExcepitonEnum;
import com.amos.doublewriterconsistence.util.InventoryKeyUtils;
import com.amos.doublewriterconsistence.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: double-writer-consistence
 * @ClassName: InventoryServiceImpl
 * @Package: com.amos.doublewriterconsistence.service
 * @author: amos
 * @Description:
 * @date: 2019/8/19 0019 下午 14:23
 * @Version: V1.0
 */
@Service
public class InventoryServiceImpl implements InventoryService {
    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    InventoryMapper inventoryMapper;

    @Autowired
    RedisUtils redisUtils;

    /**
     * 删除库存的缓存
     *
     * @param key
     * @return
     */
    @Override
    public Boolean removeInventoryCache(String key) {
        this.logger.info("移除库存：{} 的缓存", key);
        key = InventoryKeyUtils.getInventoryKey(key);
        return this.redisUtils.del(key);
    }

    /**
     * 更新数据库库存记录
     *
     * @param inventory
     */
    @Override
    public void updateInventory(Inventory inventory) {
        this.logger.info("更新库存：{} 的库存记录", inventory.getId());
        this.inventoryMapper.update(inventory);
    }

    /**
     * 保存库存的缓存记录
     *
     * @param inventory
     * @return
     */
    @Override
    public Boolean saveInventoryCache(Inventory inventory) {
        AmExcepitonEnum.NOT_NULL.assertNotEmpty(inventory);
        String key = InventoryKeyUtils.getInventoryKey(inventory.getId());
        this.logger.info("保存缓存数据的Key：{}", key);
        return this.redisUtils.save(key, inventory);
    }

    /**
     * 获取指定key的缓存值
     *
     * @param key
     * @return
     */
    @Override
    public Inventory getInventoryCache(String key) {
        key = InventoryKeyUtils.getInventoryKey(key);
        Object object = this.redisUtils.get(key);
        return JSONObject.parseObject(JSONObject.toJSONString(object), Inventory.class);
    }

    /**
     * 根据id查询库存记录
     *
     * @param id
     * @return
     */
    @Override
    public Inventory selectById(String id) {
        return this.inventoryMapper.selectById(id);
    }

    /**
     * 设置空值在缓存中的失效时间
     *
     * @param inventoryKey 键值
     * @param expireTime   失效时间
     */
    @Override
    public void saveNullForCache(String inventoryKey, long expireTime) {
        AmExcepitonEnum.NOT_NULL.assertNotEmpty(inventoryKey);
        String key = InventoryKeyUtils.getInventoryKey(inventoryKey);
        this.logger.info("保存空值，Key：{}", key);
        this.redisUtils.save(key, "", expireTime);
    }
}
