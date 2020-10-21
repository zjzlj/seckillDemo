package com.zjzlj.seckilldemo.componets;

import com.zjzlj.seckilldemo.annotation.SeckillRateLimit;
import com.zjzlj.seckilldemo.common.api.CommonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;



import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Order(1)
public class RateLimitAop {

    final String scriptFile="redis/rateLimiter.lua";
    final String rateLimitKeyPre="rateLimit";


    @Autowired
    RedisRateLimiter rateLimiter;

    @Pointcut("execution(public * com.zjzlj.seckilldemo.controller.*.*(..))")
    public void pointcut() {

    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(SeckillRateLimit.class)) {
            SeckillRateLimit seckillRateLimit = method.getAnnotation(SeckillRateLimit.class);
            long permitsPerSecond= seckillRateLimit.permitsPerSecond();
            long maxBurstSeconds = seckillRateLimit.maxBurstSeconds();
            long maxWaitSecond= seckillRateLimit.maxWaitSecond();
            String keyName = seckillRateLimit.key();

            //完整的key rateLimit:methodName:key
            String rateLimitKey;

            String methodName = method.getName();
            List<String> parameterNames = Arrays.asList(methodSignature.getParameterNames());
            Object[] args = joinPoint.getArgs();

            //获取注解key中对应方法的特定属性值
            int parameterIndex = parameterNames.indexOf(keyName);
            if(parameterIndex!=-1){
                rateLimitKey= rateLimitKeyPre+":"+methodName+":"+args[parameterIndex];
            }else {
                rateLimitKey= rateLimitKeyPre+":"+methodName;
            }


            if(! tryAcquire(permitsPerSecond,maxBurstSeconds,rateLimitKey,maxWaitSecond)){
                return CommonResult.rateLimit(null);
            }
        }

        Object result = joinPoint.proceed();
        return result;
    }

    private Boolean tryAcquire(long permitsPerSecond, long maxBurstSeconds,String rateLimitKey,long maxWaitSecond) throws InterruptedException {
        List<String> keys= Arrays.asList(rateLimitKey);
        return rateLimiter.tryAcquire(
                scriptFile,
                keys,
                String.valueOf(permitsPerSecond),
                String.valueOf(maxBurstSeconds),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(1l),
                String.valueOf(maxWaitSecond*1000));
    }
}
