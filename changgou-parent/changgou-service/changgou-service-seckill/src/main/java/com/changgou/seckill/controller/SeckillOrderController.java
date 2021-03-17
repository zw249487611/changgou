package com.changgou.seckill.controller;

import com.changgou.entity.Result;
import com.changgou.entity.SeckillStatus;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckillOrder")
@CrossOrigin
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 添加秒杀订单
     *
     * @param time
     * @param id
     */
    @RequestMapping("/add")
    public Result add(String time, Long id) {
        String username = "heima";
        seckillOrderService.add(id,time, username);
        return new Result(true, StatusCode.OK, "正在排队……");
    }

    /**
     * 抢单状态查询
     */
    @GetMapping("/query")
    public Result queryStatus() {
        String username = "heima";
        SeckillStatus seckillStatus = seckillOrderService.queryStatus(username);
        //查询成功
        if (seckillStatus != null) {
            return new Result(true, StatusCode.OK, "查询状态成功", seckillStatus);
        }
        return new Result(false, StatusCode.NOTFOUNDERROR, "抢单失败！！");
    }

}
