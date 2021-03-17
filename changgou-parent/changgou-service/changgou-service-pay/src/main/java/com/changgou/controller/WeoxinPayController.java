package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@RestController
@RequestMapping("/weixin/pay")
public class WeoxinPayController {

    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 创建支付二维码
     */
    @RequestMapping("/create/native")
    public Result createNative(@RequestParam Map<String,String> parameterMap) {

        Map<String,String> resultMap = weixinPayService.createnative(parameterMap);
        return new Result(true, StatusCode.OK, "创建二维码预付订单成功", resultMap);
    }

    /**
     * 微信支付状态查询
     */
    @GetMapping("/status/query")
    public Result queryStatus(String outtradeno) {
        //查询支付状态
        Map map = weixinPayService.qurtyStatus(outtradeno);
        return new Result(true, StatusCode.OK, "查询支付状态成功", map);

    }
    /**
     * 接收 微信支付通知的结果  结果(以流的形式传递过来)
     */
    @RequestMapping("/notify/url")
    public String notifyurl(HttpServletRequest request) throws Exception {
        try {
            //获取网络输入流
            ServletInputStream is = request.getInputStream();
            //创建一个OutputStream-》输入文件中
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            //微信支付结果的字节数据
            byte[] bytes = baos.toByteArray();
            String xmlResult = new String(bytes, "UTF-8");
            System.out.println(xmlResult);

            //XML字符串-->MAp
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            System.out.println(resultMap);

            //获取自定义参数
            String attach = resultMap.get("attach");
            Map<String, String> attachMap = JSON.parseObject(attach, Map.class);

            //发送支付结果给MQ
            rabbitTemplate.convertAndSend(attachMap.get("exchange"), attachMap.get("routingkey"), JSON.toJSONString(resultMap));
//            rabbitTemplate.convertAndSend("exchange.order","queue.order", JSON.toJSONString(xmlResult));

//            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("return_code", "SUCCESS");
            resultMap.put("return_msg", "OK");
            return WXPayUtil.mapToXml(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
