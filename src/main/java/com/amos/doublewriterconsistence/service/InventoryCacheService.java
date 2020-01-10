package com.amos.doublewriterconsistence.service;

import com.amos.doublewriterconsistence.entity.Inventory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * Copyright © 2018 嘉源锐信. All rights reserved.
 *
 * @Project: double-writer-consistence
 * @ClassName: InventoryCacheService
 * @Package: com.amos.doublewriterconsistence.service
 * @author: zhuqb
 * @Description:
 * @date: 2019/8/30 0030 下午 16:05
 * @Version: V1.0
 */
public interface InventoryCacheService {
    @Cacheable(key = "'id_'+#id", unless = "#result == null")
    Inventory select(String id);

    @CacheEvict(key = "'id_'+#id")
    void delete(String id);
}
