package com.zjzlj.seckilldemo.service;

import com.zjzlj.seckilldemo.dto.OrderMessage;

public interface SeckillService {
    public String generateRandomPath(String goodsId);

    public String preDecrStock(String path,String goodsId,Integer decrStock);

    public String generateOrder(String orderSn,String goodsId,Integer stock,String userId);
}
