package com.amos.doublewriterconsistence.util;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: InventoryKeyUtils
 * @Package: com.amos.consumer.util
 * @author: amos
 * @Description: 库存缓存
 * @date: 2019/8/19 0019 上午 9:21
 * @Version: V1.0
 */
public class InventoryKeyUtils {
    public static final String DEFAULT_INVENTORY_KEY_PREFIX = "inventory:key:";

    /**
     * 获取库存数据的key
     *
     * @param key
     * @return
     */
    public static String getInventoryKey(String key) {
        return DEFAULT_INVENTORY_KEY_PREFIX + key;
    }
}
