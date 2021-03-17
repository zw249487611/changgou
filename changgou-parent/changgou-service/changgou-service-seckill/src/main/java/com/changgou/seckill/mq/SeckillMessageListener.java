package com.changgou.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 消息监听
     */
    @RabbitHandler
    public void getMessage(String message) {
//        System.out.println(message);
        try {
            //将支付信息装成map
            Map<String, String> resultMap = JSON.parseObject(message, Map.class);

            //return_code -> 通信标识-SUCCESS
            String return_code = resultMap.get("return_code");
                //outtradeno->订单号
            String outtradeno = resultMap.get("out_trade_no");
            //自定义数据
            String attach = resultMap.get("attach");
            Map<String, String> attachMap = JSON.parseObject(attach, Map.class);

            //result_code ->业务结果-SUCCESS->修改订单状态
            if (return_code.equals("SUCCESS")) {
                String result_code = resultMap.get("result_code");
                if (result_code.equals("SECCESS")) {
                    //修改订单信息
                    seckillOrderService.updatePayStatus(attachMap.get("username"),resultMap.get("transaction+id"),resultMap.get("time_end"));

                    //清理用户排队信息
                } else {
                    //          FAIL->删除订单【真实工作中，存入musql】-》回滚库存
                    seckillOrderService.deleteOrder(attachMap.get("username"));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
