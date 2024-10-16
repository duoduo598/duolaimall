package com.powernobug.mall.order.mq;

import com.alibaba.fastjson2.JSON;
import com.powernobug.mall.mq.constant.MqTopicConst;
import com.powernobug.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.mq
 * @author: HuangWeiLong
 * @date: 2024/10/11 21:58
 */
@Slf4j
@Component
public class OrderDelayConsumer {
    @Autowired
    OrderService orderService;
    @PostConstruct
    public void init() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("order-delay-consumer");

        consumer.setNamesrvAddr("192.168.92.129:9876");

        consumer.subscribe(MqTopicConst.DELAY_ORDER_TOPIC,"*");

        consumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                log.info("接到了延迟消息！");

                try {
                    MessageExt messageExt = list.get(0);
                    byte[] body = messageExt.getBody();
                    String message = new String(body);
                    String msgId = messageExt.getMsgId();
                    Long orderId = JSON.parseObject(message, Long.class);
                    log.info("接收了延时消息：{},消息id：{}",orderId,msgId);
                    orderService.execExpiredOrder(orderId);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });
        consumer.start();
    }
}
