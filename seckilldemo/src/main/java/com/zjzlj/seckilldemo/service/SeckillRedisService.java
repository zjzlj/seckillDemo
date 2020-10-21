package com.zjzlj.seckilldemo.service;

public interface SeckillRedisService {
    Integer getStock(String goodsId);

    void setStock(String goodsId,Integer stock);

    Boolean decrStock(String goodsId, Integer stock);

    Long setRandomPath(String goodsId, String randomPath);

    Boolean IsRandomPathExisted(String goodsId, String randomPath);

    String getIncrNumber(String goodsId,String date);
}
