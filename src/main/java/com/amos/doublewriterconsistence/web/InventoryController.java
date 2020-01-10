package com.amos.doublewriterconsistence.web;

import com.amos.doublewriterconsistence.bean.Result;
import com.amos.doublewriterconsistence.entity.Inventory;
import com.amos.doublewriterconsistence.request.Request;
import com.amos.doublewriterconsistence.request.impl.InventoryCacheRequest;
import com.amos.doublewriterconsistence.request.impl.InventoryDBRequest;
import com.amos.doublewriterconsistence.service.InventoryService;
import com.amos.doublewriterconsistence.service.RequestAsyncProcessService;
import com.amos.doublewriterconsistence.util.ResultWapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: double-writer-consistence
 * @ClassName: InventoryController
 * @Package: com.amos.doublewriterconsistence.web
 * @author: amos
 * @Description: 主要测试
 * 1. 所有的请求是否从缓存队列中走
 * 2. 通过延迟数据的操作，看看读请求是否有等待
 * 3，读请求通过之后，相同的读请求是否直接返回
 * 4. 读请求的数据是否从缓存中获取
 * @date: 2019/8/19 0019 下午 15:31
 * @Version: V1.0
 */
@RestController
@RequestMapping(value = "/inventory")
public class InventoryController {
    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    InventoryService inventoryService;

    @Autowired
    RequestAsyncProcessService requestAsyncProcessService;

    /**
     * 更新库存的数据记录
     * 1. 将更新数据的记录路由到指定的队列中
     * 2. 后台不断的将从队列中取值去处理
     *
     * @param inventory
     * @return
     */
    @PostMapping(value = "/updateInventory")
    public Result updateInventory(@RequestBody Inventory inventory) {
        try {
            Request request = new InventoryDBRequest(inventory, this.inventoryService);
            this.requestAsyncProcessService.route(request);
            return ResultWapper.success();
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            this.logger.error(e.getMessage());
            return ResultWapper.error(e.getMessage());
        }
    }

    /**
     * 获取库存记录
     * 如果在在一定时间内获取不到数据，则直接从数据库中获取，并且数据写入到缓存中
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/getInventory/{id}")
    public Result getInventory(@PathVariable("id") String id) {
        this.logger.info("获取库存记录：{}", id);
        Inventory inventory = null;
        try {
            Request request = new InventoryCacheRequest(id, this.inventoryService, Boolean.FALSE);
            this.requestAsyncProcessService.route(request);
            long startTime = System.currentTimeMillis();
            long waitTime = 0L;
            // 不断循环从缓存中获取数据
            // 如果在在一定时间内获取不到数据，则直接从数据库中获取，并且数据写入到缓存中
            while (true) {
                if (waitTime > 3000) {
                    break;
                }
                inventory = this.inventoryService.getInventoryCache(id);
                if (null != inventory) {
                    this.logger.info("从缓存中获取到数据");
                    return ResultWapper.success(inventory);
                } else {
                    Thread.sleep(20);
                    waitTime = System.currentTimeMillis() - startTime;
                }

            }

            // 直接从数据库中获取数据
            inventory = this.inventoryService.selectById(id);
            if (null != inventory) {
                request = new InventoryCacheRequest(id, this.inventoryService, Boolean.TRUE);
                this.requestAsyncProcessService.route(request);
                return ResultWapper.success(inventory);
            }
            return ResultWapper.error("查询不到数据");
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            this.logger.error(e.getMessage());
            return ResultWapper.error(e.getMessage());
        }
    }
}
