package com.itheima.weixin;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信SDK相关测试
 */

public class WeixinUtilTest {
    /**
     * 1、生成随机字符
     * 2、将Map转成XML字符串
     */
    @Test
    public void testDemo() throws Exception {
        //随机字符串
        String str = WXPayUtil.generateNonceStr();
        System.out.println("随机字符串微信:"+str);
        //Map转成XML字符串
        Map<String, String> dataMap = new HashMap<String,String>();
        dataMap.put("id","id001");
        dataMap.put("title","小苹美食支付");
        dataMap.put("money","998");
        String toXml = WXPayUtil.mapToXml(dataMap);
        System.out.println("xml字符串：\n" + toXml);
        //将map转成xml字符串，并且生成签名
        String signedXml = WXPayUtil.generateSignedXml(dataMap, "xiaoping");
        System.out.println("xml字符串带有签名：\n" + signedXml);

        //将xml字符串装成map
        Map<String, String> mapResult = WXPayUtil.xmlToMap(signedXml);
        System.out.println("将xml转成map:\n" + mapResult);

    }
}
