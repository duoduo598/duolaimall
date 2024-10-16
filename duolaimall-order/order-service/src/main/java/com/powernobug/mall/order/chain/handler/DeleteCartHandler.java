package com.powernobug.mall.order.chain.handler;

import com.powernobug.mall.order.chain.AbstractOrderHandler;
import com.powernobug.mall.order.chain.SaveOrderContext;
import com.powernobug.mall.order.client.CartApiClient;
import com.powernobug.mall.order.query.OrderDetailParam;
import com.powernobug.mall.order.query.OrderInfoParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.chain.handler
 * @author: HuangWeiLong
 * @date: 2024/10/16 21:27
 */
@Component
public class DeleteCartHandler extends AbstractOrderHandler {
    @Autowired
    CartApiClient cartApiClient;
    @Override
    public void handle(SaveOrderContext saveOrderContext) {
        String userId = saveOrderContext.getUserId();
        OrderInfoParam orderInfoParam = saveOrderContext.getOrderInfoParam();
        List<OrderDetailParam> orderDetailList = orderInfoParam.getOrderDetailList();


        List<Long> skuIdList = orderDetailList.stream().map(OrderDetailParam::getSkuId).collect(Collectors.toList());
        cartApiClient.removeCartProductsInOrder(userId,skuIdList);
        if(next!=null){
            next.handle(saveOrderContext);
        }
    }
}
