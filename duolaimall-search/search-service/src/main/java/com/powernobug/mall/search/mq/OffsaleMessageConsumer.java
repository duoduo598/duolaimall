package com.powernobug.mall.search.mq;

import com.alibaba.fastjson2.JSON;
import com.powernobug.mall.mq.constant.MqTopicConst;
import com.powernobug.mall.search.service.SearchService;
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
 * @package: com.powernobug.mall.search.mq
 * @author: HuangWeiLong
 * @date: 2024/10/10 22:25
 */
@Slf4j
@Component
public class OffsaleMessageConsumer {
    @Autowired
    SearchService searchService;
    @PostConstruct
    public void init() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("search-offsale-service");

        consumer.setNamesrvAddr("192.168.92.129:9876");

        consumer.subscribe(MqTopicConst.PRODUCT_OFFSALE_TOPIC,"*");

        consumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                log.info("接收到了下架消息！");

                try {
                    MessageExt messageExt = list.get(0);
                    byte[] body = messageExt.getBody();
                    String s = new String(body);
                    Long skuId = JSON.parseObject(s, Long.class);
                    String msgId = messageExt.getMsgId();
                    log.info("接收到了消息：{},消息id为{}",skuId,msgId);
                    searchService.lowerGoods(skuId);
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
