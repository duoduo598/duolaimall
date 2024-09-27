package com.powernobug.mall.product.runner;

import com.powernobug.mall.common.constant.RedisConst;
import com.powernobug.mall.product.service.SkuService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.runner
 * @author: HuangWeiLong
 * @date: 2024/9/27 21:44
 */
@Component
public class BloomFilterRunner implements ApplicationRunner {

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    SkuService skuService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long expectedInsertions = 10000;            // 数据规模
        double falseProbability = 0.01;             // 误判率
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        boolean ret = bloomFilter.tryInit(expectedInsertions, falseProbability);
        System.out.println("布隆过滤器初始化了,初始化的结果是:" + ret);



        // 接下来需要往布隆过滤器中添加元素
        // 什么时候添加？
        // 添加哪些元素?

        // 有两种方案：
        // 1. 在布隆过滤器初始化成功之后，查询出数据库中所有已经上架的商品Id，然后存储布隆过滤器
        // 2. 在后台管理员用户 点击【上架】的时候，把对应的商品id添加到布隆过滤器中

        // 查询所有上架状态的商品
        List<Long> skuIds = skuService.findAllOnSaleProducts();
        for (Long skuId : skuIds) {
            bloomFilter.add(skuId);
            System.out.println("往布隆过滤器中添加了一个元素: "  + skuId);
        }
    }
}
