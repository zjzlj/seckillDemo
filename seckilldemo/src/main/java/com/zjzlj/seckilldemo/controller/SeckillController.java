package com.zjzlj.seckilldemo.controller;

import com.zjzlj.seckilldemo.annotation.SeckillRateLimit;
import com.zjzlj.seckilldemo.common.api.CommonResult;
import com.zjzlj.seckilldemo.service.SeckillRedisService;
import com.zjzlj.seckilldemo.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @RequestMapping("/randomPath")
    @ResponseBody
    @SeckillRateLimit(permitsPerSecond = 1000,maxBurstSeconds = 1,maxWaitSecond = 1, key = "goodsId")
    public CommonResult randomPath(@RequestParam("goodsId")  String goodsId){
        /**
         * 1. redis库存>0
         * 2. 产生随机路径
         */
        String randomPath = seckillService.generateRandomPath(goodsId);

        return CommonResult.success(randomPath);
    }


    @RequestMapping("/{randomPath}/decrStock")
    @ResponseBody
    public CommonResult decrStock(@PathVariable("path") String path,
                                  @RequestParam("goodsId")  String goodsId,
                                  @RequestParam("decrStock")  Integer decrStock){
        /**
         * 1. 验证随机路径
         * 2. redis预减库存
         * 3. 发送至rabbitmq
         * 3. rabbitmq接收 数据库减库存
         */
        String orderSn = seckillService.preDecrStock(path, goodsId, decrStock);
        return CommonResult.success(orderSn);
    }
}
