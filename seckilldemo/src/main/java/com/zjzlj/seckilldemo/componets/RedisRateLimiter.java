package com.zjzlj.seckilldemo.componets;


import com.zjzlj.seckilldemo.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class RedisRateLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRateLimiter.class);


    @Autowired
    RedisService redisService;



    /**
     * 在规定时间内 尝试获取tokenNums 个令牌
     * @param scriptFile
     * @param permitsPerSecond
     * @param maxBurstSeconds
     * @param now
     * @param tokenNums
     * @param maxBurstSeconds
     * @return
     * @throws InterruptedException
     */
    public Boolean tryAcquire(String scriptFile, List<String> keys, String permitsPerSecond, String maxBurstSeconds, String now, String tokenNums, String maxWaitMills) throws InterruptedException{
        //目前lua脚本突然返回数组形式 如 [1] 不知道原因
        Long milliToWait=Long.valueOf(redisService.lua(scriptFile, keys,permitsPerSecond,maxBurstSeconds,now,tokenNums,maxWaitMills).replace("[","").replace("]",""));

        if(milliToWait==-1){
           return false;
       }

        Thread.sleep(milliToWait);
        return true;
    }





}
