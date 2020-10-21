package com.zjzlj.seckilldemo;

import com.zjzlj.seckilldemo.componets.RedisRateLimiter;
import com.zjzlj.seckilldemo.service.SeckillRedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class SeckilldemoApplicationTests {

    @Autowired
    RedisRateLimiter rateLimiter;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    SeckillRedisService seckillRedisService;

    final String scriptFile="redis/rateLimiter.lua";
    final String rateLimitKeyPre="rateLimit";

    @Test
    void contextLoads() throws InterruptedException {
        seckillRedisService.setStock("1",5);
        seckillRedisService.decrStock("1",3);
    }

}
