package com.powernobug.mall.ware.api;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.order.dto.OrderInfoDTO;
import com.powernobug.mall.ware.api.dto.WareOrderTaskDTO;
import com.powernobug.mall.ware.api.dto.WareSkuDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "service-order")
public interface OrderApiClient {


    /**
     * 根据Id获取订单信息
     */
    @GetMapping("/api/order/inner/getOrderInfo/{orderId}")
    OrderInfoDTO getOrderInfoDTOByOrderId(@PathVariable(value = "orderId") Long orderId);

    /**
     * 拆单
     */
    @PostMapping("/api/order/inner/orderSplit/{orderId}")
    List<WareOrderTaskDTO> orderSplit(@PathVariable(value = "orderId") String orderId, @RequestBody List<WareSkuDTO> wareSkuDTOList);

    /**
     * 库存扣减完成，修改订单状态
     */
    @PostMapping("/api/order/inner/successLockStock/{orderId}/{taskStatus}")
    Result successLockStock(@PathVariable(value = "orderId") String orderId, @PathVariable(value = "taskStatus") String taskStatus);

}
