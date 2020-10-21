package com.zjzlj.seckilldemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderMessage {
    String orderSn;
    String goodsId;
    Integer stock;
    String userId;
}
