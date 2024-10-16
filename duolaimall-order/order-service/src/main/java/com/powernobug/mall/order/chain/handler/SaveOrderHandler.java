package com.powernobug.mall.order.chain.handler;

import com.powernobug.mall.order.chain.AbstractOrderHandler;
import com.powernobug.mall.order.chain.SaveOrderContext;
import com.powernobug.mall.order.converter.OrderInfoConverter;
import com.powernobug.mall.order.model.OrderInfo;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.chain.handler
 * @author: HuangWeiLong
 * @date: 2024/10/16 21:28
 */
@Component
public class SaveOrderHandler extends AbstractOrderHandler {
    @Autowired
    OrderInfoConverter orderInfoConverter;
    @Autowired
    OrderService orderService;
    @Override
    public void handle(SaveOrderContext saveOrderContext) {
        String userId = saveOrderContext.getUserId();
        OrderInfoParam orderInfoParam = saveOrderContext.getOrderInfoParam();
        OrderInfo convertedOrderInfoParam = orderInfoConverter.convertOrderInfoParam(orderInfoParam);
        convertedOrderInfoParam.setUserId(Long.valueOf(userId));
        Long orderId = orderService.saveOrderInfo(convertedOrderInfoParam);
        saveOrderContext.setOrderId(orderId);
        if(next!=null){
            next.handle(saveOrderContext);
        }
    }
}
