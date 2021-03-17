package com.changgou.entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

public class FeignInterceptor implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate template) {
        /**
         * 获取用户的令牌
         */
        //记录了当前用户请求的所有数据，包含请求头和请求参数等
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //获取请求头中的数据
            //获取所有头的名字
        //用户当前请求的时候，是在同一个线程中的请求，如果开启了熔断，默认就是线程池的隔离，会开启新的线程，需要将熔断策略改下哦
        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
        while (headerNames.hasMoreElements()) {
            //请求头的key
            String headerKey = headerNames.nextElement();
            //获取请求头的值
            String headerValue = requestAttributes.getRequest().getHeader(headerKey);
            System.out.println(headerKey + ":" + headerValue);

            //将请求头信息封装到头中，使用feign调用的时候，回传递给下一个微服务
            template.header(headerKey, headerValue);

        }
    }
}
