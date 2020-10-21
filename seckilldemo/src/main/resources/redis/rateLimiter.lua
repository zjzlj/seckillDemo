-- KEYS[1]  string  限流器令牌的key

-- ARGV[1]  int     每秒放入的令牌数
-- ARGV[2]  int     最大存储maxBurstSeconds秒生成的令牌
-- ARGV[3]  int     当前时间戳
local permitsPerSecond = tonumber(ARGV[1])
local maxBurstSeconds = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local tokenNum= tonumber(ARGV[4])
local maxWaitMillis= tonumber(ARGV[5])


local permitsKey= KEYS[1]
local maxPermitsKey= permitsKey..":maxPermits"
local storedPermitsKey= permitsKey..":storedPermits"
local intervalMillisKey= permitsKey..":intervalMillis"
local nextFreeTicketMillisKey= permitsKey..":nextFreeTicketMillis"

local maxPermits
local storedPermits
local intervalMillis
local nextFreeTicketMillis



local function permits()
    maxPermits = permitsPerSecond * maxBurstSeconds
    storedPermits = permitsPerSecond
    intervalMillis = math.ceil(1000 / permitsPerSecond)
    nextFreeTicketMillis = now;
end

--redis的过期时长
local function expires()
    return 120+math.ceil((math.max(nextFreeTicketMillis,now)-now)/1000);
end

--异步更新当前持有的令牌数
--若当前时间晚于nextFreeTicketMicros，则计算该段时间内可以生成多少令牌，将生成的令牌加入令牌桶中并更新数据
local function reSync()
    if now>nextFreeTicketMillis then
        storedPermits = math.min(maxPermits, math.ceil(storedPermits + (now - nextFreeTicketMillis) / intervalMillis));
        nextFreeTicketMillis = now;
        return true;
    end

    return false

end


--更新令牌桶
local function setPermits()
     local reset_time=expires()
     redis.call('set', maxPermitsKey, maxPermits, 'EX', reset_time)
     redis.call('set', storedPermitsKey, storedPermits, 'EX', reset_time)
     redis.call('set', intervalMillisKey, intervalMillis, 'EX', reset_time)
     redis.call('set', nextFreeTicketMillisKey, nextFreeTicketMillis, 'EX', reset_time)
end

--获取令牌桶
local function getPermits()
    maxPermits = tonumber(redis.call("get", maxPermitsKey))
    storedPermits = tonumber(redis.call("get", storedPermitsKey))
    intervalMillis = tonumber(redis.call("get", intervalMillisKey))
    nextFreeTicketMillis = tonumber(redis.call("get", nextFreeTicketMillisKey))

    if not storedPermits then
        permits()
        setPermits()
    end
end



--返回获取{tokens}个令牌最早可用的时间
local function queryEarliestAvailable(tokens)
    getPermits()
    reSync()

     --可以消耗的令牌数
     local storedPermitsToSpend = math.min(tokens, storedPermits)
     --需要等待的令牌数
     local freshPermits = tokens - storedPermitsToSpend
     --需要等待的时间
     local waitMillis = freshPermits * intervalMillis

     return nextFreeTicketMillis - now + waitMillis;
end

--在等待的时间内是否可以获取到令牌
local function  canAcquire(tokens, timeoutMills)
    return queryEarliestAvailable(tokens)<= timeoutMills
end

--预定@{tokens}个令牌并返回所需要等待的时间
local function  reserveAndGetWaitLength(tokens)
    getPermits()
    reSync()

    --可以消耗的令牌数
    local storedPermitsToSpend = math.min(tokens, storedPermits);
    --需要等待的令牌数
    local freshPermits = tokens - storedPermitsToSpend
    --需要等待的时间
    local waitMillis = freshPermits * intervalMillis
    --设置新状态Permits
    nextFreeTicketMillis=nextFreeTicketMillis + waitMillis
    storedPermits= storedPermits- storedPermitsToSpend
    setPermits()

    return  nextFreeTicketMillis - now;
end

--tokens 要获取的令牌数
--timeout 获取令牌等待的时间，负数被视为0
local function tryAcquire(tokens, timeoutMills)
    timeoutMills = math.max(timeoutMills, 0);
    local milliToWait

    if not canAcquire(tokens,timeoutMills) then
        return -1
    else
       return reserveAndGetWaitLength(tokens)
    end
end








return tryAcquire(tokenNum,maxWaitMillis)




