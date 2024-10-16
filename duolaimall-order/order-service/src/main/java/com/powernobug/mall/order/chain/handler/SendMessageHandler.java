package com.powernobug.mall.order.chain.handler;

import com.powernobug.mall.common.constant.ResultCodeEnum;
import com.powernobug.mall.common.execption.BusinessException;
import com.powernobug.mall.mq.constant.MqTopicConst;
import com.powernobug.mall.mq.producer.BaseProducer;
import com.powernobug.mall.order.chain.AbstractOrderHandler;
import com.powernobug.mall.order.chain.SaveOrderContext;
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
public class SendMessageHandler extends AbstractOrderHandler {
    @Autowired
    BaseProducer baseProducer;
    @Override
    public void handle(SaveOrderContext saveOrderContext) {
        Long orderId = saveOrderContext.getOrderId();
        Boolean ret = baseProducer.sendDelayMessage(MqTopicConst.DELAY_ORDER_TOPIC, orderId, MqTopicConst.DELAY_ORDER_LEVEL);
        if(!ret){
            throw new BusinessException("发送消息失败", ResultCodeEnum.FAIL.getCode());
        }
        if(next!=null){
            next.handle(saveOrderContext);
        }
    }
}
