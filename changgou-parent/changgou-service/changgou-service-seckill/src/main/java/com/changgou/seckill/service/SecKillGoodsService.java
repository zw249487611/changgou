package com.changgou.seckill.service;

import com.changgou.seckill.pojo.SeckillGoods;

import java.util.List;

public interface SecKillGoodsService {

    List<SeckillGoods> list(String time);

    /**
     * 根据时间和秒杀商品ID查询秒杀商品数据
     *
     * @param time
     * @param id
     */
    SeckillGoods one(String time, Long id);

}
