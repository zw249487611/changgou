package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderService;
import com.changgou.user.feign.UserFeign;
import com.changgou.util.IdWorker;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/****
 * @Author:传智播客
 * @Description:Order业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 删除（逻辑）订单，回滚库存
     * @param outtradeno:
     */
    @Override
    public void deleteOrder(String outtradeno) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(outtradeno);
        order.setUpdateTime(new Date());
        order.setPayStatus("2");//支付失败
        //修改倒数据库中
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 修改订单状态
     * 1、修改支付时间
     * 2、修改支付状态
     *
     * @param outtradeno          :订单号
     * @param paytime:支付时间
     * @param transactionid:交易流水号
     */
    @Override
    public void updateStatus(String outtradeno, String paytime, String transactionid) throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date payTimeInfo = dateFormat.parse(paytime);
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(outtradeno);
        //修改订单信息
        order.setPayTime(payTimeInfo);
        order.setPayStatus("1");//支付状态
        order.setTransactionId(transactionid);//交易流水号

        //修改倒数据库中
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * Order条件+分页查询
     * @param order 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Order> findPage(Order order, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(order);
        //执行搜索
        return new PageInfo<Order>(orderMapper.selectByExample(example));
    }

    /**
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Order> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Order>(orderMapper.selectAll());
    }

    /**
     * Order条件查询
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order){
        //构建查询条件
        Example example = createExample(order);
        //根据构建的条件查询数据
        return orderMapper.selectByExample(example);
    }


    /**
     * Order构建查询对象
     * @param order
     * @return
     */
    public Example createExample(Order order){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(order!=null){
            // 订单id
            if(!StringUtils.isEmpty(order.getId())){
                    criteria.andEqualTo("id",order.getId());
            }
            // 数量合计
            if(!StringUtils.isEmpty(order.getTotalNum())){
                    criteria.andEqualTo("totalNum",order.getTotalNum());
            }
            // 金额合计
            if(!StringUtils.isEmpty(order.getTotalMoney())){
                    criteria.andEqualTo("totalMoney",order.getTotalMoney());
            }
            // 优惠金额
            if(!StringUtils.isEmpty(order.getPreMoney())){
                    criteria.andEqualTo("preMoney",order.getPreMoney());
            }
            // 邮费
            if(!StringUtils.isEmpty(order.getPostFee())){
                    criteria.andEqualTo("postFee",order.getPostFee());
            }
            // 实付金额
            if(!StringUtils.isEmpty(order.getPayMoney())){
                    criteria.andEqualTo("payMoney",order.getPayMoney());
            }
            // 支付类型，1、在线支付、0 货到付款
            if(!StringUtils.isEmpty(order.getPayType())){
                    criteria.andEqualTo("payType",order.getPayType());
            }
            // 订单创建时间
            if(!StringUtils.isEmpty(order.getCreateTime())){
                    criteria.andEqualTo("createTime",order.getCreateTime());
            }
            // 订单更新时间
            if(!StringUtils.isEmpty(order.getUpdateTime())){
                    criteria.andEqualTo("updateTime",order.getUpdateTime());
            }
            // 付款时间
            if(!StringUtils.isEmpty(order.getPayTime())){
                    criteria.andEqualTo("payTime",order.getPayTime());
            }
            // 发货时间
            if(!StringUtils.isEmpty(order.getConsignTime())){
                    criteria.andEqualTo("consignTime",order.getConsignTime());
            }
            // 交易完成时间
            if(!StringUtils.isEmpty(order.getEndTime())){
                    criteria.andEqualTo("endTime",order.getEndTime());
            }
            // 交易关闭时间
            if(!StringUtils.isEmpty(order.getCloseTime())){
                    criteria.andEqualTo("closeTime",order.getCloseTime());
            }
            // 物流名称
            if(!StringUtils.isEmpty(order.getShippingName())){
                    criteria.andEqualTo("shippingName",order.getShippingName());
            }
            // 物流单号
            if(!StringUtils.isEmpty(order.getShippingCode())){
                    criteria.andEqualTo("shippingCode",order.getShippingCode());
            }
            // 用户名称
            if(!StringUtils.isEmpty(order.getUsername())){
                    criteria.andLike("username","%"+order.getUsername()+"%");
            }
            // 买家留言
            if(!StringUtils.isEmpty(order.getBuyerMessage())){
                    criteria.andEqualTo("buyerMessage",order.getBuyerMessage());
            }
            // 是否评价
            if(!StringUtils.isEmpty(order.getBuyerRate())){
                    criteria.andEqualTo("buyerRate",order.getBuyerRate());
            }
            // 收货人
            if(!StringUtils.isEmpty(order.getReceiverContact())){
                    criteria.andEqualTo("receiverContact",order.getReceiverContact());
            }
            // 收货人手机
            if(!StringUtils.isEmpty(order.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",order.getReceiverMobile());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(order.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",order.getReceiverAddress());
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(!StringUtils.isEmpty(order.getSourceType())){
                    criteria.andEqualTo("sourceType",order.getSourceType());
            }
            // 交易流水号
            if(!StringUtils.isEmpty(order.getTransactionId())){
                    criteria.andEqualTo("transactionId",order.getTransactionId());
            }
            // 订单状态 
            if(!StringUtils.isEmpty(order.getOrderStatus())){
                    criteria.andEqualTo("orderStatus",order.getOrderStatus());
            }
            // 支付状态 0:未支付 1:已支付
            if(!StringUtils.isEmpty(order.getPayStatus())){
                    criteria.andEqualTo("payStatus",order.getPayStatus());
            }
            // 发货状态 0:未发货 1:已发货 2:已送达
            if(!StringUtils.isEmpty(order.getConsignStatus())){
                    criteria.andEqualTo("consignStatus",order.getConsignStatus());
            }
            // 是否删除
            if(!StringUtils.isEmpty(order.getIsDelete())){
                    criteria.andEqualTo("isDelete",order.getIsDelete());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Order
     * @param order
     */
    @Override
    public void update(Order order){



        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 增加Order
     * @param order
     */
    @Override
    public void add(Order order){
        //订单添加一次
        order.setId(String.valueOf(idWorker.nextId()));//主键ID

        //获取订单明细->购物车集合
        List<OrderItem> orderItems = null;//redisTemplate.boundHashOps("Cart_" + order.getUsername()).values();
        //获取勾选的商品Id,需要下单的商品，将要下单的商品的ID信息从购物车中移除
        for (Long skuId : order.getSkuIds()) {
            orderItems.add((OrderItem) redisTemplate.boundHashOps("Cart_" + order.getUsername()).get(skuId));
            redisTemplate.boundHashOps("Cart_" + order.getUsername()).delete(skuId);
        }

        int totalNum = 0;//订单总数量
        int totalMoney = 0;//订单总金额
        //封装MAP,封装递减数据
        Map<String, Integer> decrmap = new HashMap<String,Integer>();

        for (OrderItem orderItem : orderItems) {
            totalNum += orderItem.getNum();
            totalMoney += orderItem.getMoney();
            //订单明细的id
            orderItem.setId(String.valueOf(idWorker.nextId()));
            //订单明细所属的订单
            orderItem.setOrderId(order.getId());

            //是否退货
            orderItem.setIsReturn("0");

            //封装递减数据
            decrmap.put(orderItem.getSkuId(), orderItem.getNum());

        }


        order.setCreateTime(new Date());//创建时间
        order.setUpdateTime(order.getCreateTime()); //更新时间
        order.setSourceType("1");//订单来源，1、web,
        order.setOrderStatus("0"); //0未支付
        order.setPayStatus("0"); //0未支付
        order.setIsDelete("0"); //未删除

        /**
         * 订单购买商品总数量=每个商品的总数量之和
         *          获取订单明细->购物车和集合
         *          循环订单明细， 每个商品的购买数量叠加
         */
        order.setTotalNum(totalNum);//订单购买总数量
        order.setTotalMoney(totalMoney);//订单总金额
        order.setPayMoney(totalMoney); //实付金额
        //添加订单信息
        orderMapper.insertSelective(order);
        //明细添加多次.循环添加订单明细信息
        for (OrderItem orderItem : orderItems) {
            orderItemMapper.insertSelective(orderItem);
        }
        //库存递减
        skuFeign.decrCount(decrmap);

        //添加用户积分活跃度。+1
        userFeign.addPoints(1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("创建订单时间：" + simpleDateFormat.format(new Date()));
        //添加订单
        rabbitTemplate.convertAndSend("orderDelayQueue", (Object) order.getId().toString(), new MessagePostProcessor() {

            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //延时读取设置
                message.getMessageProperties().setExpiration("10000");
                return message;
            }
        });
    }

    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
    @Override
    public Order findById(String id){
        return  orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Order全部数据
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }
}
