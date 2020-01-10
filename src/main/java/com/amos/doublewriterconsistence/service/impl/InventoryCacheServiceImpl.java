package com.amos.doublewriterconsistence.service.impl;

import com.amos.doublewriterconsistence.entity.Inventory;
import com.amos.doublewriterconsistence.mapper.InventoryMapper;
import com.amos.doublewriterconsistence.service.InventoryCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: double-writer-consistence
 * @ClassName: InventoryCacheServiceImpl
 * @Package: com.amos.doublewriterconsistence.service.impl
 * @author: zhuqb
 * @Description: Ehcache 缓存
 * @date: 2019/8/30 0030 下午 16:06
 * @Version: V1.0
 */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
@CacheConfig(cacheNames = "inventory")
public class InventoryCacheServiceImpl implements InventoryCacheService {

    @Autowired
    InventoryMapper inventoryMapper;

    /**
     * 从数据库中查询数据，并且加载数据到缓存
     * condition满足缓存条件的数据才会放入缓存，condition在调用方法之前和之后都会判断
     * unless用于否决缓存更新的，不像condition，该表达只在方法执行之后判断，此时可以拿到返回值result进行判断了
     *
     * @param id
     * @return
     */
    @Override
    @Cacheable(key = "'id_'+#id", unless = "#result == null")
    public Inventory select(String id) {
        return this.inventoryMapper.selectById(id);
    }

    /**
     * 数据删除的时候，也删除堆缓存
     *
     * @param id
     */
    @Override
    @CacheEvict(key = "'id_'+#id")
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void delete(String id) {
        this.inventoryMapper.delete(id);
    }
}
