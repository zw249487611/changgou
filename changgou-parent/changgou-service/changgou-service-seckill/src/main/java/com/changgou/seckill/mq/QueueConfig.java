package com.changgou.seckill.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1、延时超时队列-》负责数据暂时存储，queue1
 * 2、真正监听的消息队列              queue2
 * 3、创建交换机
 */
@Configuration
public class QueueConfig {
    /**
     * 延时超时队列-》负责数据暂时存储queue1
     */
    @Bean
    public Queue delaySeckillQueue() {
        return QueueBuilder.durable("delaySeckillQueue")
                        .withArgument("x-dead-letter-exchange","seckillExchange")//当前队列的消息一旦过期，则进入死信队列交换机
                        .withArgument("x-dead-letter-routing-key","SeckillQueue") //将私信队列的数据路由到指定队列中
                        .build();
    }
    /**
     * 真正监听的消息队列              queue2
     */
    @Bean
    public Queue seckillQueue() {
        return new Queue("SeckillQueue");
    }

    /**
     * 秒杀交换机
     * @return
     */
    @Bean
    public Exchange seckillExchange() {
        return new DirectExchange("seckillExchange");
    }

    /**
     * 队列绑定交换机
     */
    @Bean
    public Binding seckillQueueBindingExchange(Queue seckillQueue, Exchange seckillExchange) {
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with("seckillQueue").noargs();
    }
}
