运营推广部门某次策划上线秒杀或者优惠活动，经测试人员估算压力测试，大约在一个小时内进来100万+用户访问，系统吞吐量固定的情况下，为保障Java服务端正常运行不崩溃，需要对正常访问用户进行限流处理，大约每秒响应1000个请求。

请问限流的系统如何设计，给出具体的实现？（服务端框架采用spring boot+mybatis+redis）





**分布式令牌桶原理与流程：**

*一. 分布式令牌桶数据结构*

通过permitsPerSecond（每秒钟生成的令牌） 和  maxBurstSeconds（最大存储maxBurstSeconds秒生成的令牌）可以生成令牌桶，  并确定分布式令牌桶的以下四个参数 ， 该参数存储与redis中

```lua
local maxPermits  --最大令牌数   permitsPerSecond * maxBurstSeconds
local storedPermits  --存储令牌数 permitsPerSecond
local intervalMillis --生成一个令牌所需要的毫秒数 math.ceil(1000 / permitsPerSecond)
local nextFreeTicketMillis  --下一次更新令牌桶的时间戳 初始时为当前时间戳
```



*二. 尝试获取令牌所需要的参数*

```lua
local permitsPerSecond = tonumber(ARGV[1])      --每秒钟生成的令牌
local maxBurstSeconds = tonumber(ARGV[2])       --maxBurstSeconds
local now = tonumber(ARGV[3])                   --当前时间戳
local tokenNum= tonumber(ARGV[4])               --想要获取的令牌数
local maxWaitMillis= tonumber(ARGV[5])          -- 最大等待时间 若获取tokenNum个令牌的时间大于此 则失败
```



*三. 一次获取令牌的流程（通过lua脚本实现 以保证原子性）*

1. 尝试从redis中获取令牌桶，若不存在 则创建令牌桶，并储存

2.  异步的更新当前令牌桶持有的令牌数， 若当前时间晚于nextFreeTicketMicros，则计算该段时间内可以生成多少令牌，将生成的令牌加入令牌桶中并更新数据

   ```lua
   local function reSync()
       if now>nextFreeTicketMillis then
           -- 最大存储令牌数 和 （新生成令牌+已有令牌） 取最小值
           storedPermits = math.min(maxPermits, math.ceil(storedPermits + (now - nextFreeTicketMillis) / intervalMillis));
           --更新nextFreeTicketMillis为当前时间戳
           nextFreeTicketMillis = now;
           return true;
       end
   
       return false
   
   end
   ```



3.  计算获取tokens个令牌最早可用的时间 并与最大等待时间比较 判断能否获取令牌

   ```lua
   local function queryEarliestAvailable(tokens)
       getPermits() --从redis中获取令牌桶 即步骤1
       reSync()  --异步的更新当前令牌桶持有的令牌数
       
        --可以消耗的令牌数
        local storedPermitsToSpend = math.min(tokens, storedPermits)
        --需要等待的令牌数
        local freshPermits = tokens - storedPermitsToSpend
        --需要等待的时间
        local waitMillis = freshPermits * intervalMillis
   
        --返回需等待时间
        return nextFreeTicketMillis - now + waitMillis;
   end
   ```



4. 若能够在maxWaitMillis时间内获取所需令牌，则更新令牌桶的信息，并返回等待时间数

   ```lua
   local function  reserveAndGetWaitLength(tokens)
       getPermits() --从redis中获取令牌桶 即步骤1
       reSync() --异步的更新当前令牌桶持有的令牌数
   
       --可以消耗的令牌数
       local storedPermitsToSpend = math.min(tokens, storedPermits);
       --需要等待的令牌数
       local freshPermits = tokens - storedPermitsToSpend
       --需要等待的时间
       local waitMillis = freshPermits * intervalMillis
       
       --设置新状态Permits
       --更新下一次更新令牌桶的时间戳
       nextFreeTicketMillis=nextFreeTicketMillis + waitMillis
       --更新以存储令牌数
       storedPermits= storedPermits- storedPermitsToSpend
       --存储令牌桶到redis
       setPermits()
   
       --返回等待时间
       return  nextFreeTicketMillis - now;
   end
   ```



5. 尝试获取令牌的线程 休眠相应时间 成功获得令牌 （本次消费为本次负责）





整体流程概览：

```lua
--tokens 要获取的令牌数
--timeout 获取令牌等待的时间，负数被视为0
local function tryAcquire(tokens, timeoutMills)
    timeoutMills = math.max(timeoutMills, 0);
    local milliToWait
	
	--判断能否在timeoutMills时间内获取tokens个令牌
    if not canAcquire(tokens,timeoutMills) then
        return -1
    else
        --获取tokens个令牌 更新令牌桶到redis 返回等待时间
       return reserveAndGetWaitLength(tokens)
    end
end
```















**限流基本流程：**

1.  创建一个基于guava的RateLimiter原理实现的分布式令牌桶， 令牌桶的相关信息存储在redis中

   ```java
   @Component
   public class RedisRateLimiter {
    public Boolean tryAcquire(args...){
        	// ...简化代码
        
        	//调用lua脚本尝试获取令牌桶中的令牌
           Long milliToWait=redisService.lua(scriptFile,keys,permitsPerSecond...);
   
           if(milliToWait==-1){
              return false;
          }
   		//成功获取令牌 休眠对应时间
           Thread.sleep(milliToWait);
           return true;
       }
   }
   ```

2. 将自定义的限流注解 标注在对应的接口上 （本系统 为/randomPath 接口）   限流注解中的参数可控制限流的频率与范围

```java
public @interface SeckillRateLimit {
    long permitsPerSecond() default 1000;

    long maxBurstSeconds() default 1;

    long maxWaitSecond() default 1;

    String key() default "";  //确定redis中令牌桶的key值   key值为被限流接口的参数名称 之后在aop可以获得其对应值 
```



3. 在aop中拦截限流注解，尝试获取分布式令牌桶中的令牌，成功则正常执行接口逻辑，否则返回失败结果。

```java
@Aspect
public class RateLimitAop {
    //简化代码
    
    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint){
        // ... 省略无关逻辑
        
         if (method.isAnnotationPresent(SeckillRateLimit.class)) {
              SeckillRateLimit seckillRateLimit = method.getAnnotation(SeckillRateLimit.class);
             //获取SeckillRateLimit注解的参数 省略  该参数作为tryAcquire的参数 用于获取令牌
             
             //尝试获取令牌  若失败 则直接返回失败结果
            if(! tryAcquire(permitsPerSecond,maxBurstSeconds,rateLimitKey,maxWaitSecond)){
                return CommonResult.rateLimit(null);
            }
         }
        
        //正常执行接口逻辑
        Object result = joinPoint.proceed();
        return result;
    }
}
```











**秒杀基本流程：**

1).用户在秒杀页面点击购买按钮

2).调用（/randomPath） 用于生成随机链接 并保存到redis中 返回随机链接 

```java
@RequestMapping("/randomPath")
@ResponseBody
@SeckillRateLimit(permitsPerSecond = 1000,maxBurstSeconds = 1,maxWaitSecond = 1, key = "goodsId")
public CommonResult randomPath(@RequestParam("goodsId")  String goodsId){
    // 简化逻辑 加入service层的具体代码
    
    /**
     * 1. redis库存>0
     * 2. 产生随机路径
     */
    
    //2. 产生随机路径
    String randomPath= MD5Util.md5(UUID.randomUUID().toString());
    //存储到redis
    seckillRedisService.setRandomPath(goodsId,randomPath);

    return CommonResult.success(randomPath);
}
```

3).带着上个接口生成的随机链接，来访问实际的秒杀接口 ( /{randomPath}/decrStock )。  先判断randomPath在redis中是否存在，  之后则是在redis中 进行预减库存， 防止超卖。 之后产生一个orderSn ， 将orderSn，商品id，购买数量，用户id发送到 rabbitmq。 返回orderSn

```java
@RequestMapping("/{randomPath}/decrStock")
@ResponseBody
public CommonResult decrStock(@PathVariable("path") String path,
                              @RequestParam("goodsId")  String goodsId,
                              @RequestParam("decrStock")  Integer decrStock){
    =// 简化逻辑 加入service层的具体代码
    /**
     * 1. 验证随机路径
     * 2. redis预减库存
     * 3. 发送至rabbitmq
     * 3. rabbitmq接收 数据库减库存
     */     
    //验证随机路径    
    Boolean isExisted = seckillRedisService.IsRandomPathExisted(goodsId, path);
    if(!isExisted){
        Asserts.Fail("without randomPath in redis!!!");  //异常统一有controllerAdvice处理
    }

    //redis预减库存  秒杀商品库存事先添加到redis中 redis本身串行操作 安全保障
    Boolean isFull = seckillRedisService.decrStock(goodsId, decrStock);
    if(!isFull){
        Asserts.Fail("without stock!!!"); //异常统一有controllerAdvice处理
    }

    //生成订单编号 非主键   利用redis incr 确保唯一
    String orderSn = generateOrderSn(goodsId);

    //此处应通过 在userService中的getCurrentUser方法  实际上是从如spring security context 中获取用户信息
    String userId= "userId_test123";
    
	//发送至rabbitmq
    orderSender.sendMessage(orderSn,goodsId,decrStock,userId);
    
    return CommonResult.success(orderSn);
}
```

4） rabbitmq消费者 进行实际的数据库操作，如扣库存，插入订单。 扣库存会通过cas来实现。

```java
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
```



