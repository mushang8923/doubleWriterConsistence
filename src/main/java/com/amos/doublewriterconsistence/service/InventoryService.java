package com.amos.doublewriterconsistence.service;

import com.amos.doublewriterconsistence.entity.Inventory;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: InventoryService
 * @Package: com.amos.common.service
 * @author: amos
 * @Description:
 * @date: 2019/8/19 0019 上午 10:40
 * @Version: V1.0
 */
public interface InventoryService {
    /**
     * 删除库存对应的缓存
     *
     * @param key
     * @return
     */
    Boolean removeInventoryCache(String key);

    /**
     * 更新库存记录
     *
     * @param inventory
     */
    void updateInventory(Inventory inventory);

    /**
     * 保存库存缓存记录
     *
     * @param inventory
     * @return
     */
    Boolean saveInventoryCache(Inventory inventory);

    /**
     * 获取库存缓存
     *
     * @param key
     * @return
     */
    Inventory getInventoryCache(String key);

    /**
     * 根据Id查询库存记录
     *
     * @param id
     * @return
     */
    Inventory selectById(String id);

    /**
     * 保存空的进入缓存 并且设定失效的时间
     *
     * @param inventoryKey 键值
     * @param expireTime   失效时间
     */
    void saveNullForCache(String inventoryKey, long expireTime);
}
