package com.powernobug.mall.promo.task;

import com.powernobug.mall.promo.service.PromoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.promo.task
 * @author: HuangWeiLong
 * @date: 2024/10/16 20:15
 */
@Slf4j
@Component
@EnableScheduling
public class PromoTask {
    @Autowired
    PromoService promoService;
    @Scheduled(cron = "0/5 * * * * ?")
    public void taskInitCache(){
        log.info("本地库存状态位初始化定时任务开始了......");
        promoService.importIntoRedis();
        log.info("本地库存状态位初始化定时任务结束了......");
    }
}
