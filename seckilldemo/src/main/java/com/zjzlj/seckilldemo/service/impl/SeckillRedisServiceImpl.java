package com.zjzlj.seckilldemo.service.impl;

import com.zjzlj.seckilldemo.service.RedisService;
import com.zjzlj.seckilldemo.service.SeckillRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SeckillRedisServiceImpl implements SeckillRedisService {
    final String  seckillStockKey="seckillStock";
    final String  seckillRandomPathKey="seckillRandomPath";
    final String  seckillOrderSnIncrKey="seckillOrderSnIncr";
    final String scriptFile="redis/seckillDecrStock.lua";


    @Autowired
    RedisService redisService;


    @Override
    public Integer getStock(String goodsId) {
        String key=seckillStockKey+":"+goodsId;
        return (Integer) redisService.get(key);
    }

    @Override
    public void setStock(String goodsId, Integer stock) {
        String key=seckillStockKey+":"+goodsId;
        redisService.set(key,stock);
    }

    @Override
    //lua 预减库存 大于0成功
    public Boolean decrStock(String goodsId, Integer stock) {
        String key=seckillStockKey+":"+goodsId;
        List<String> keys= Arrays.asList(key);
        Boolean result = Boolean.valueOf(redisService.lua(scriptFile, keys, String.valueOf(stock)).replace("[", "").replace("]", ""));

        return result;
    }

    @Override
    public Long setRandomPath(String goodsId, String randomPath) {
        String key=seckillRandomPathKey+":"+goodsId;
        Long result = redisService.sAdd(key, randomPath);
        redisService.expire(key,30*1000);
        return result;

    }

    @Override
    public Boolean IsRandomPathExisted(String goodsId, String randomPath) {
        String key=seckillRandomPathKey+":"+goodsId;
        Boolean result=redisService.sIsMember(key,randomPath);
        if(result){
            redisService.sRemove(key,randomPath);
        }

        return result;
    }

    @Override
    public String getIncrNumber(String goodsId,String date) {
        //生成6位以上自增id
        String key = seckillOrderSnIncrKey+":"+date+":"+goodsId ;
        Long incr = redisService.incr(key, 1);
        redisService.expire(key,1000*3600*24);
        String incrStr = incr.toString();
        return incrStr;
    }
}
