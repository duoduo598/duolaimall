package com.powernobug.mall.promo.client;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.order.query.OrderInfoParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-order")
public interface OrderApiClient {

    /**
     * 提交秒杀订单
     * @param orderInfo
     * @return
     */
    @PostMapping("/api/order/inner/seckill/submitOrder")
    Result submitOrder(@RequestBody OrderInfoParam orderInfo);
}
