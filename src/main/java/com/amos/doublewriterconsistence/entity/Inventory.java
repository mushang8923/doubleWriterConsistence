package com.amos.doublewriterconsistence.entity;

/**
 * Copyright © 2018 五月工作室. All rights reserved.
 *
 * @Project: rabbitmq
 * @ClassName: Inventory
 * @Package: com.amos.common.entity
 * @author: amos
 * @Description:
 * @date: 2019/8/19 0019 上午 10:45
 * @Version: V1.0
 */
public class Inventory {
    private String id;
    private Integer count;
    private String name;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
