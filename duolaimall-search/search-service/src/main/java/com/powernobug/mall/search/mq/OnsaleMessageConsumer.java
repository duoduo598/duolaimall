package com.powernobug.mall.search.mq;

import com.alibaba.fastjson2.JSON;
import com.powernobug.mall.mq.constant.MqTopicConst;
import com.powernobug.mall.search.controller.SearchApiController;
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
 * @date: 2024/10/10 22:24
 */
@Slf4j
@Component
public class OnsaleMessageConsumer {
    @Autowired
    SearchService searchService;
    @PostConstruct
    public void init() throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer("search-onsale-service");

        defaultMQPushConsumer.setNamesrvAddr("192.168.92.129:9876");

        defaultMQPushConsumer.subscribe(MqTopicConst.PRODUCT_ONSALE_TOPIC,"*");
        
        defaultMQPushConsumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                log.info("接收到上架消息！");
                try {
                    MessageExt messageExt = list.get(0);
                    byte[] body = messageExt.getBody();
                    String nowObject = new String(body);
                    Long skuId = JSON.parseObject(nowObject, Long.class);
                    String msgId = messageExt.getMsgId();
                    log.info("接收到了消息：{},消息id为{}",skuId,msgId);
                    searchService.upperGoods(skuId);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });
        defaultMQPushConsumer.start();
    }
}
