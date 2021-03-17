package com.changgou.order.service.impl;

import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;
    /**
     * 加入购物车
     * @param num
     * @param id
     */
    @Override
    public void add(Integer num, String id,String username) {
        //当添加购物车数量<=0的时候，需要移除该商品信息
        if (num <= 0) {
            //移除购物车该商品
            redisTemplate.boundHashOps("Cart_" + username).delete(id);
            //如果此时购物车数量为空，则连购物车一起移除
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            if (size == null || size <= 0) {
                redisTemplate.delete("Cart_" + username);
            }
        }
        //1.查询商品详情.需要feign,查询远程的商品
            //1.1先查询Sku
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();

        //1.2在查询spu
        Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
        Spu spu = spuResult.getData();

        OrderItem orderItem = createOrderItem(num, id, sku, spu);

        //3.将购物车数据存入到redis:namespace->username
        redisTemplate.boundHashOps("Cart_" + username).put(id, orderItem);
    }

    /**
     * 购物车集合查询
     * @param username : 用户登录名
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        return redisTemplate.boundHashOps("Cart_" + username).values();
    }

    //抽取封装信息的方法，
    private OrderItem createOrderItem(Integer num, String id, Sku sku, Spu spu) {
        //2、将加入购物车的商品信息封装到OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * orderItem.getPrice());
        orderItem.setImage(spu.getImage());
        return orderItem;
    }
}
