package com.amos.doublewriterconsistence.mapper;

import com.amos.doublewriterconsistence.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Package com.amos.common.mapper
 * @ClassName InventoryMapper
 * @Description TODO
 * @Author Amos
 * @Modifier
 * @Date 2019/8/18 22:14
 * @Version 1.0
 **/
@Mapper
public interface InventoryMapper {
    /**
     * 根据ID查询数据
     *
     * @param id
     * @return
     */
    Inventory selectById(@Param("id") String id);

    /**
     * 更新库存数据
     *
     * @param inventory
     */
    void update(@Param("record") Inventory inventory);

    /**
     * 删除
     *
     * @param id
     */
    void delete(String id);
}
