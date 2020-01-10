package com.amos.doublewriterconsistence.web;

import com.amos.doublewriterconsistence.entity.Inventory;
import com.amos.doublewriterconsistence.service.InventoryCacheService;
import com.amos.doublewriterconsistence.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: double-writer-consistence
 * @ClassName: InventoryCacheController
 * @Package: com.amos.doublewriterconsistence.web
 * @author: zhuqb
 * @Description:
 * @date: 2019/9/5 0005 下午 18:23
 * @Version: V1.0
 */
@RestController
public class InventoryCacheController {

    @Autowired
    InventoryService inventoryService;

    @Autowired
    InventoryCacheService inventoryCacheService;

    /**
     * nginx定向请求
     * 1. 先从redis中查询，
     * 2. 如果redis中查询不到，则从ehcache中查询
     * 3. ehcache中查询不到，则从数据库总查询(这步包括在ehcache中)
     *
     * @param id
     * @return
     */
    @GetMapping("/load")
    public Inventory load(@RequestParam("id") String id) {
        // 先从redis中查询
        Inventory inventory = this.inventoryService.getInventoryCache(id);
        if (StringUtils.isEmpty(inventory)) {
            inventory = this.inventoryCacheService.select(id);
        }
        return inventory;
    }
}
