package com.zjzlj.seckilldemo.componets;

import com.zjzlj.seckilldemo.dto.OrderMessage;
import com.zjzlj.seckilldemo.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = {"orderQueue"})
public class OrderReceiver {
    private static Logger LOGGER = LoggerFactory.getLogger(OrderReceiver.class);

    @Autowired
    SeckillService seckillService;

    @RabbitHandler
    public void handle(OrderMessage orderMessage){
        String orderSn = orderMessage.getOrderSn();
        String goodsId = orderMessage.getGoodsId();
        Integer stock = orderMessage.getStock();
        String userId = orderMessage.getUserId();

        seckillService.generateOrder(orderSn,goodsId,stock,userId);

    }
}
