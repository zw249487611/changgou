package com.changgou.seckill.service.impl;

import com.changgou.entity.SeckillStatus;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.seckill.task.MultiThreadingCreateOrder;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {



    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 删除订单
     * @param username
     */
    @Override
    public void deleteOrder(String username) {
        //删除订单
        redisTemplate.boundHashOps("SeckillOrder").delete(username);
        //查询用户排队信息，
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        //删除排队信息
        clearUserQueue(username);

        //回滚库存->redis递增，->redis中不一定有商品
        String namespace = "SeckillGoods_" +seckillStatus.getTime();
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(namespace).get(seckillStatus.getGoodsId());
        //如果商品为空，
        if (seckillGoods == null) {
            //数据库中查询
            seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
            //更新数据库的库存
            seckillGoods.setStockCount(1);
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
        } else {
            seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
        }
        redisTemplate.boundHashOps(namespace).put(seckillGoods.getId(), seckillGoods);

        //队列中
        redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGoods.getId()).leftPush(seckillGoods.getId());


    }

    /**
     * 修改订单状态
     * @param username
     * @param transactionid
     * @param endtime
     */
    @Override
    public void updatePayStatus(String username, String transactionid, String endtime) {
        //先查询订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);

        if (seckillOrder != null) {
            try {
                //修改订单状态信息
                seckillOrder.setStatus("1");
                seckillOrder.setTransactionId(transactionid);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date parTimeInfo = simpleDateFormat.parse(endtime);
                seckillOrder.setPayTime(parTimeInfo);
                //同步到数据库
                seckillOrderMapper.insertSelective(seckillOrder);
                //删除redis中的订单
                redisTemplate.boundHashOps("SeckillOrder").delete(username);
                //删除用户排队信息
                clearUserQueue(username);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }
    /**
     * 清理用户排队抢单信息
     */
    public void clearUserQueue(String username) {
        //排队标识
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //排队信息清理掉
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }

    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size) {
        return null;
    }

    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size) {
        return null;
    }

    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public void update(SeckillOrder seckillOrder) {

    }

    @Override
    public void add(SeckillOrder seckillOrder) {

    }

    @Override
    public SeckillOrder findById(Long id) {
        return null;
    }

    @Override
    public List<SeckillOrder> findAll() {
        return null;
    }

    /**
     * 秒杀下单--排队
     * @param id
     * @param time
     * @param username
     * @return
     */
    @Override
    public boolean add(Long id, String time, String username) {
        //记录用户排队的次数
        /**
         * 1、key
         * 2、自增的值
         */
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        if (userQueueCount > 1) {
            //100标识为重复排队的错误码
            throw new RuntimeException("100");
        }

        //创建排队对象
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(),1, id, time);
        //list是队列类型,用户抢单排队
        redisTemplate.boundListOps("SeckillorderQueue").leftPush(seckillStatus);
        //用户抢单状态，用于查询
        redisTemplate.boundHashOps("SeckillStatus").put(username, seckillStatus);
        //异步执行
        multiThreadingCreateOrder.createOrder();

        return true;
    }

    /**
     * 抢单状态查询
     * @param username
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }
}
