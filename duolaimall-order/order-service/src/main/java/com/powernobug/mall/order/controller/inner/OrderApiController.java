package com.powernobug.mall.order.controller.inner;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.order.converter.OrderInfoConverter;
import com.powernobug.mall.order.dto.OrderInfoDTO;
import com.powernobug.mall.order.service.OrderService;
import com.powernobug.mall.ware.api.dto.WareOrderTaskDTO;
import com.powernobug.mall.ware.api.dto.WareSkuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.controller.inner
 * @author: HuangWeiLong
 * @date: 2024/10/11 22:21
 */
@RestController
public class OrderApiController {
    @Autowired
    OrderService orderService;
    @GetMapping("/api/order/inner/getOrderInfo/{orderId}")
    public OrderInfoDTO getOrderInfoDTO(@PathVariable(value = "orderId") Long orderId){
        return orderService.getOrderInfo(orderId);
    }


    // 支付成功，修改订单状态
    @PostMapping("/api/order/inner/success/{orderId}")
    public Result successPay(@PathVariable(value = "orderId") Long orderId){
        orderService.successPay(orderId);
        return Result.ok();
    }

    /**
     * 拆单
     */
    @PostMapping("/api/order/inner/orderSplit/{orderId}")
    public List<WareOrderTaskDTO> orderSplit(@PathVariable(value = "orderId") String orderId, @RequestBody List<WareSkuDTO> wareSkuDTOList){
        return orderService.orderSplit(orderId, wareSkuDTOList);
    }

    /**
     * 库存扣减完成，修改订单状态
     */
    @PostMapping("/api/order/inner/successLockStock/{orderId}/{taskStatus}")
    public Result successLockStock(@PathVariable(value = "orderId") String orderId, @PathVariable(value = "taskStatus") String taskStatus){
        orderService.successLockStock(orderId,taskStatus);
        return Result.ok();
    }

}
