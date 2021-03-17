package com.changgou.order.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 延时队列配置
 */
@Configuration
public class QueueConfig {
    /**
     * 创建Queue1,延时队列，会过期，过期后，将数据发给Queue2
     */
    @Bean
    public Queue orderDelagQueue() {
        return QueueBuilder.durable("orderListenerQueue")
                .withArgument("x-dead-letter-exchange", "orderListenerExchange")  //orderDelayQueue队列信息会过期，过期之后，进入倒死信队列，死信交换机数据绑定倒其他交换机
                .withArgument("x-dead-letter-routing-key", "orderListenerQueue")
                .build();

    }
    /**
     * 创建Queue2
     */
    @Bean
    public Queue orderListenerQueue() {
        return new Queue("orderDelagQueue",true);
    }

    /**
     * 创建交换机
     */
    @Bean
    public Exchange orderListenerExchange() {
        return new DirectExchange("orderListenerExchange");
    }

    /**
     * 队列queue2绑定Exchange
     */
    @Bean
    public Binding orderListenerBinding(Queue orderListenerQueue,Exchange orderListenerExchange) {
        return BindingBuilder.bind(orderListenerQueue).to(orderListenerExchange).with("orderListenerQueue").noargs();
    }

}
