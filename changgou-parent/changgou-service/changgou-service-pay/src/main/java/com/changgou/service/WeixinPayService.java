package com.changgou.service;

import java.util.Map;

public interface WeixinPayService {
    /**
     * 获取二维码
     */
    public Map createnative(Map<String, String> parameterMap);

    /**
     * 查询微信支付状态
     */
    Map qurtyStatus(String outtradeno);
}
