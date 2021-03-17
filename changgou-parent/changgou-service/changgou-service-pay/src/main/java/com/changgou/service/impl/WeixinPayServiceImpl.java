package com.changgou.service.impl;


import com.alibaba.fastjson.JSON;
import com.changgou.entity.HttpClient;
import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${weixin.appid}")
    private String appId;

    @Value("${weixin.partner}")
    private String partner;

    @Value("${weixin.partnerkey}")
    private String partnerkey;

    @Value("${weixin.notifyurl}")
    private String notifyUrl;

    /**
     * 生成二维码
     * @param parameterMap
     * @return
     */
    @Override
    public Map createnative(Map<String, String> parameterMap) {
        try {
            //参数
            Map<String, String> map = new HashMap<String, String>();
            map.put("body", "腾讯充值中心-QQ会员充值");    //商品描述
            map.put("appid", appId);      //应用id
            map.put("mch_id", partner);      //应用id

            //随机字符串
            map.put("nonce_str", WXPayUtil.generateNonceStr());
            //订单号
            map.put("out_trade_no", parameterMap.get("outtradeno"));
            //交易金额，单位：分
            map.put("total_fee", parameterMap.get("totalfee"));
            //终端机器号
            map.put("spbill_create_ip", "127.0.0.1");
            //交易结果回调通知地址
            map.put("notify_url", notifyUrl);
            map.put("trade_type", "NATIVE");
            //获取自定义数据
            String exchange = parameterMap.get("exchange");
            String routingkey = parameterMap.get("routingkey");
            Map<String, String> attachMap = new HashMap<String,String>();
            attachMap.put("exchange", exchange);
            attachMap.put("routingkey", routingkey);
            //如果是秒杀订单，需要串username
            String username = parameterMap.get("username");
            if (StringUtils.isNotEmpty(username)) {
                attachMap.put("username", username);
            }

            String attach = JSON.toJSONString(attachMap);
            map.put("attach", attach);

            //Map转成XML字符串，可以带签名

            String signedXml = WXPayUtil.generateSignedXml(map, partnerkey);

            //2.Url地址
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            HttpClient httpClient = new HttpClient(url);
            //提交方式
            httpClient.setHttps(true);

            //提交参数
            httpClient.setXmlParam(signedXml);
            //执行请求
            httpClient.post();
            //获取返回的数据

            String result = httpClient.getContent();

            //返回数据转成Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            return resultMap;

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 查询微信支付状态
     * @param outtradeno
     * @return
     */
    @Override
    public Map qurtyStatus(String outtradeno) {
        try {
            //参数
            Map<String, String> map = new HashMap<String, String>();
            map.put("body", "腾讯充值中心-QQ会员充值");    //商品描述
            map.put("appid", appId);      //应用id
            map.put("mch_id", partner);      //应用id

            map.put("out_trade_no", outtradeno);
            //Map转成XML字符串，可以带签名

            String signedXml = WXPayUtil.generateSignedXml(map, partnerkey);

            //2.Url地址
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            HttpClient httpClient = new HttpClient(url);
            //提交方式
            httpClient.setHttps(true);

            //提交参数
            httpClient.setXmlParam(signedXml);
            //执行请求
            httpClient.post();
            //获取返回的数据

            String result = httpClient.getContent();

            //返回数据转成Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            return resultMap;

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
