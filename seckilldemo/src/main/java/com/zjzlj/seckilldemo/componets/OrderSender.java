package com.zjzlj.seckilldemo.componets;

import com.zjzlj.seckilldemo.dto.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderSender {
    private static Logger LOGGER = LoggerFactory.getLogger(OrderSender.class);

    @Autowired
    private AmqpTemplate amqpTemplate;

    final private String exchange= "orderExchange";

    final private String routeKey= "orderKey";

    public void sendMessage(String orderSn,String goodsId,Integer stock,String userId){
        amqpTemplate.convertAndSend(exchange, routeKey, new OrderMessage(orderSn,goodsId,stock,userId));
    }
}
