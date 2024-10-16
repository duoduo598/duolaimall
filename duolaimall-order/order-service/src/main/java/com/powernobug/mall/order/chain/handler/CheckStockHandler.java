package com.powernobug.mall.order.chain.handler;

import com.powernobug.mall.common.constant.ResultCodeEnum;
import com.powernobug.mall.common.execption.BusinessException;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.order.chain.AbstractOrderHandler;
import com.powernobug.mall.order.chain.SaveOrderContext;
import com.powernobug.mall.order.client.WareApiClient;
import com.powernobug.mall.order.query.OrderDetailParam;
import com.powernobug.mall.order.query.OrderInfoParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.chain.handler
 * @author: HuangWeiLong
 * @date: 2024/10/16 21:26
 */
@Component
public class CheckStockHandler extends AbstractOrderHandler {
    @Autowired
    WareApiClient wareApiClient;
    @Override
    public void handle(SaveOrderContext saveOrderContext) {
        OrderInfoParam param1 = saveOrderContext.getOrderInfoParam();
        List<OrderDetailParam> orderDetailList = param1.getOrderDetailList();
        for (OrderDetailParam param : orderDetailList) {
            Long skuId = param.getSkuId();
            String skuName = param.getSkuName();
            Integer skuNum = param.getSkuNum();
            Result result = wareApiClient.hasStock(skuId, skuNum);

            Integer code = ResultCodeEnum.SUCCESS.getCode();
            if(!code.equals(result.getCode())){
                throw new BusinessException(skuName+"库存不足！",ResultCodeEnum.FAIL.getCode());
            }
        }
        if(next!=null){
            next.handle(saveOrderContext);
        }
    }
}
