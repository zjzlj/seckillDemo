package com.zjzlj.seckilldemo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    //订单队列,交换器及路由键
    @Bean
    public Queue orderQueue(){
        Queue queue= QueueBuilder
                .durable("orderQueue")
                .build();
        return queue;
    }

    @Bean
    public DirectExchange orderExchange(){
        DirectExchange exchange= ExchangeBuilder
                .directExchange("orderExchange")
                .durable(true)
                .build();
        return exchange;
    }

    @Bean
    public Binding orderTtlBinding(){
        Binding binding= BindingBuilder
                .bind(orderQueue())
                .to(orderExchange())
                .with("orderKey");
        return binding;
    }
}
