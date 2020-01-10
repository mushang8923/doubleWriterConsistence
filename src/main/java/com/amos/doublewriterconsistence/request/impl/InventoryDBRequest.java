package com.amos.doublewriterconsistence.request.impl;

import com.amos.doublewriterconsistence.entity.Inventory;
import com.amos.doublewriterconsistence.request.Request;
import com.amos.doublewriterconsistence.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Package com.amos.consumer.service.impl
 * @ClassName InventoryServiceImpl
 * @Description 数据更新操作
 * 1. 先删除缓存中的数据
 * 2. 再更新数据库中的数据
 * @Author Amos
 * @Modifier
 * @Date 2019/8/18 22:16
 * @Version 1.0
 **/
public class InventoryDBRequest implements Request {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Inventory inventory;

    private InventoryService inventoryService;

    /**
     * 构造器
     *
     * @param inventory
     * @param inventoryService
     */
    public InventoryDBRequest(Inventory inventory, InventoryService inventoryService) {
        this.inventory = inventory;
        this.inventoryService = inventoryService;
    }

    /**
     * 库存数据库操作
     * 1. 先删除缓存中对应的数据
     * 2. 更新数据库中的数据
     */
    @Override
    public void process() {
        this.logger.info("数据库操作——移除缓存中的数据");
        // 首先删除缓存中的数据
        this.inventoryService.removeInventoryCache(this.inventory.getId());
        // 为了测试 所以这里操作时间长点
        try {
            this.logger.info("数据库操作——等待3秒操作");
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 再更新数据库中的数据
        this.logger.info("数据库操作——更新数据库中的数据");
        this.inventoryService.updateInventory(this.inventory);
    }

    /**
     * 接口返回库存记录的ID
     *
     * @return
     */
    @Override
    public String getInventoryId() {
        return this.inventory.getId();
    }

    /**
     * 始终不更新
     *
     * @return
     */
    @Override
    public Boolean isForceRefresh() {
        return Boolean.FALSE;
    }
}
