--
-- Created by IntelliJ IDEA.
-- User: zjzlj
-- Date: 2020/10/19
-- Time: 20:03
-- To change this template use File | Settings | File Templates.
--

local decrStock= tonumber(ARGV[1])


local seckillStockKey= KEYS[1]

if tonumber(redis.call("get",seckillStockKey))>decrStock then
    redis.call("decrby",seckillStockKey,decrStock)
    return tostring(true)
else
    return tostring(false)
end

