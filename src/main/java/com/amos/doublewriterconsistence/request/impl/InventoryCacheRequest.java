package com.amos.doublewriterconsistence.request.impl;

import com.alibaba.fastjson.JSONObject;
import com.amos.doublewriterconsistence.entity.Inventory;
import com.amos.doublewriterconsistence.request.Request;
import com.amos.doublewriterconsistence.service.InventoryService;
import com.amos.doublewriterconsistence.util.InventoryKeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: InventoryCacheRequestImpl
 * @Package: com.amos.common.request.impl
 * @author: amos
 * @Description: 处理缓存的业务请求
 * 缓存这边我们需要在数据库中查询出对应的数据，然后将数据写入到缓存中
 * 由此我们需要获取库存的id，根据id获取库存的数据
 * 然后将库存数据写入到缓存中 数据中的key是库存ID的标识，value是查询出来的缓存数据
 * @date: 2019/8/19 0019 上午 8:59
 * @Version: V1.0
 */
public class InventoryCacheRequest implements Request {
    public final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 库存的id
     */
    private String inventoryId;
    private InventoryService inventoryService;
    /**
     * 是否需要更新缓存
     * 数据更新该值是false
     */
    private Boolean isForceFresh;

    public InventoryCacheRequest(String inventoryId, InventoryService inventoryService, Boolean isForceFresh) {
        this.inventoryId = inventoryId;
        this.inventoryService = inventoryService;
        this.isForceFresh = isForceFresh;
    }

    /**
     * 1. 根据id到数据库中查询对应的库存数据
     * 2. 查询到了则将数据保存到缓存中
     * 3. 如果查询不到的话则将对应的空数据保存到缓存中，并且设置失效时间
     * 这里的查询不到数据也保存到缓存中，主要是为了防止恶意请求，以防通过不断的循环一个查找不到记录的id来不断的请求数据库，给数据库造成了访问压力，占用系统的资源
     * 同时，也给缓存数据设置失效时间，方便数据发生变化时，及时提供变更后的数据
     */
    @Override
    public void process() {
        // 首先从数据库中查询对应的库存数据
        Inventory inventory = this.inventoryService.selectById(this.inventoryId);
        this.logger.info("库存缓存操作——查询数据库数据:" + JSONObject.toJSONString(inventory));
        if (StringUtils.isEmpty(inventory)) {
            // 查询不到数据的话，对应的key存储空字符串，并且设置失效时间
            this.inventoryService.saveNullForCache(InventoryKeyUtils.getInventoryKey(this.inventoryId), 10000);
        } else {
            this.logger.info("库存缓存操作——保存缓存数据:" + JSONObject.toJSONString(inventory));
            this.inventoryService.saveInventoryCache(inventory);
        }
    }


    @Override
    public String getInventoryId() {
        return this.inventoryId;
    }

    @Override
    public Boolean isForceRefresh() {
        return this.isForceFresh;
    }
}
