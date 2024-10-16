package com.powernobug.mall.order.chain.handler;

import com.powernobug.mall.common.constant.ResultCodeEnum;
import com.powernobug.mall.common.execption.BusinessException;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.order.chain.AbstractOrderHandler;
import com.powernobug.mall.order.chain.SaveOrderContext;
import com.powernobug.mall.order.client.ProductApiClient;
import com.powernobug.mall.order.query.OrderDetailParam;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.chain.handler
 * @author: HuangWeiLong
 * @date: 2024/10/16 21:26
 */
@Component
public class CheckPriceHandler extends AbstractOrderHandler {
    @Autowired
    ProductApiClient productApiClient;
    @Autowired
    OrderService orderService;
    @Override
    public void handle(SaveOrderContext saveOrderContext) {
        String userId = saveOrderContext.getUserId();
        OrderInfoParam param1 = saveOrderContext.getOrderInfoParam();
        List<OrderDetailParam> orderDetailList = param1.getOrderDetailList();

        for (OrderDetailParam param : orderDetailList) {
            Long skuId = param.getSkuId();
            BigDecimal skuPrice = productApiClient.getSkuPrice(skuId);
            BigDecimal orderPrice = param.getOrderPrice();
            if(!orderPrice.equals(skuPrice)){
                orderService.refreshPrice(skuId,userId);
                throw new BusinessException(param.getSkuName() + "价格发生变动", ResultCodeEnum.FAIL.getCode());
            }
        }
        if(next!=null){
            next.handle(saveOrderContext);
        }
    }
}
