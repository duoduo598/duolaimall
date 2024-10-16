package com.powernobug.mall.promo.redis;

import com.powernobug.mall.common.constant.RedisConst;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
public class RedisStockOper {

    @Autowired
    RedissonClient redissonClient;

    private final static String STOCK_LUA_NAME = "Redis扣减库存脚本";
    /*
        -- 调用Redis的get指令，查询活动库存，其中KEYS[1]为传入的参数1，即库存key
        local c_s = redis.call('get', KEYS[1])
        -- 判断活动库存是否充足，其中ARGV[1]为传入当前抢购数量
        if not c_s or tonumber(c_s) < tonumber(ARGV[1]) then
           return -1
        end
        -- 如果活动库存充足，则进行扣减操作。其中ARGV[1]为传入当前抢购数量
        redis.call('decrby',KEYS[1], ARGV[1])
    * */
    private final static String STOCK_LUA = "local c_s = redis.call('hget', KEYS[1], ARGV[1]) \n" +
            "if not c_s or tonumber(c_s) < tonumber(ARGV[2]) then \n" +
            "return -1 end \n" +
            "return redis.call('hincrby',KEYS[1], ARGV[1], -tonumber(ARGV[2]))";

    private String sha1;

    @PostConstruct
    public void loadScript(){
        // 缓存脚本
        sha1 = redissonClient.getScript().scriptLoad(STOCK_LUA);
        log.info("load script {}", sha1);
    }

    public Long decrRedisStock(Long skuId, Integer count) {
        // 执行脚本，扣减商品skuId对应的库存，扣减的值为count
        return redissonClient.getScript(StringCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE,
                sha1,
                RScript.ReturnType.INTEGER,
                List.of(RedisConst.PROMO_SECKILL_GOODS_STOCK), // 脚本不涉及键，传递空列表
                skuId.toString(), count.toString());
    }

}
