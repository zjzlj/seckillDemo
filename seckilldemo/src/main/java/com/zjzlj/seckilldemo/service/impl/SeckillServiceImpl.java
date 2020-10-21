package com.zjzlj.seckilldemo.service.impl;

import com.zjzlj.seckilldemo.common.exception.Asserts;
import com.zjzlj.seckilldemo.common.utils.MD5Util;
import com.zjzlj.seckilldemo.componets.OrderSender;
import com.zjzlj.seckilldemo.dto.OrderMessage;
import com.zjzlj.seckilldemo.service.SeckillRedisService;
import com.zjzlj.seckilldemo.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    SeckillRedisService seckillRedisService;

    @Autowired
    OrderSender orderSender;


    @Override
    public String generateRandomPath(String goodsId) {

        //1. redis库存>0
        Integer stock = seckillRedisService.getStock(goodsId);
        if(stock<=0){
            Asserts.Fail("without stock!!!");
        }

        //2. 产生随机路径
        String randomPath= MD5Util.md5(UUID.randomUUID().toString());
        seckillRedisService.setRandomPath(goodsId,randomPath);

        return randomPath;
    }

    @Override
    public String preDecrStock(String path,String goodsId,Integer decrStock) {
        /**
         * 1. 验证随机路径
         * 2. redis预减库存
         * 3. 发送至rabbitmq
         * 3. rabbitmq接收 数据库减库存
         */

        Boolean isExisted = seckillRedisService.IsRandomPathExisted(goodsId, path);
        if(!isExisted){
            Asserts.Fail("without randomPath in redis!!!");
        }

        Boolean isFull = seckillRedisService.decrStock(goodsId, decrStock);
        if(!isFull){
            Asserts.Fail("without stock!!!");
        }

        String orderSn = generateOrderSn(goodsId);

        //此处应通过 在userService中的getCurrentUser方法  实际上是从如spring security context 中获取用户信息
        String userId= "userId_test123";

        orderSender.sendMessage(orderSn,goodsId,decrStock,userId);

        return orderSn;
    }


    /**
     * 此处未rabiitmq消费者具体处理生产订单的函数
     * 具体订单逻辑不在本次考虑
     * 主要解决超卖问题
     */
    @Override
    @Transactional
    public String generateOrder(String orderSn,String goodsId,Integer stock,String userId) {

        /*
            超卖主要通过之前redis预减   在数据库中主要通过cas来保证 为数据库表添加版本号

            select version from seckillgoods where id=#{goodsId}
            update seckillgoods set stock=stock-#{stock}, version=version+1 where stock>=#{stock} and version=#{version}
         */

        return null;
    }


    /**
     * 生成18位订单编号:8位日期+goodsId+6位以上自增id
     */
    private String generateOrderSn(String goodsId) {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        //生成6位以上自增id
        String incrStr =seckillRedisService.getIncrNumber(goodsId,date.substring(4));
        
        StringBuilder sb = new StringBuilder();
        sb.append(date);
        sb.append(goodsId);
        if(incrStr.length()<6){
            sb.append(String.format("06d",incrStr));
        }else{
            sb.append(incrStr);
        }

        String orderSn = sb.toString();
        return orderSn;
    }
}
