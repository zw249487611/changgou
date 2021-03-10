package com.changgou.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.changgou.search.feign")
@EnableEurekaClient
public class WebSearchApplication {
    public static void main(String[] args) {
//        System.setProperty("es.set.netty.runtime.available.processors","false");
        SpringApplication.run(WebSearchApplication.class, args);
    }

}
