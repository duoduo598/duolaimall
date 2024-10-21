package com.powernobug.mall.promo.controller;

import com.powernobug.mall.promo.service.PromoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.promo.controller
 * @author: HuangWeiLong
 * @date: 2024/10/20 22:29
 */
@Slf4j
@RestController
public class TestController {
    @Autowired
    PromoService promoService;
    @GetMapping("init/cache")
    public void taskInitCache(){
        log.info("本地库存状态位初始化定时任务开始了......");
        promoService.importIntoRedis();
        log.info("本地库存状态位初始化定时任务结束了......");
    }
    @GetMapping("promo/finish")
    public void promoFinished() {

        log.info("活动结束，清空缓存...");
        promoService.clearRedisCache();
    }

}
