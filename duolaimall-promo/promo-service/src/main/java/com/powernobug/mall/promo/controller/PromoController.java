package com.powernobug.mall.promo.controller;

import com.powernobug.mall.common.constant.RedisConst;
import com.powernobug.mall.common.constant.ResultCodeEnum;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.common.util.AuthContext;
import com.powernobug.mall.common.util.MD5;
import com.powernobug.mall.order.dto.OrderTradeDTO;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.promo.api.dto.SeckillGoodsDTO;
import com.powernobug.mall.promo.constant.SeckillCodeEnum;
import com.powernobug.mall.promo.constant.SeckillGoodsStockStatus;
import com.powernobug.mall.promo.service.PromoService;
import com.powernobug.mall.promo.util.LocalCacheHelper;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.promo.controller
 * @author: HuangWeiLong
 * @date: 2024/10/17 19:49
 */
@RestController
public class PromoController {
    @Autowired
    PromoService promoService;
    @Autowired
    RedissonClient redissonClient;
    @GetMapping("/seckill")
    public Result getSeckillGoodsList(){
        List<SeckillGoodsDTO> seckillGoodsDTOS = promoService.findAll();
        return Result.ok(seckillGoodsDTOS);
    }
    @GetMapping("/seckill/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId){
        return Result.ok(promoService.getSeckillGoodsDTO(skuId));
    }
    @GetMapping("/seckill/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable Long skuId, HttpServletRequest request){
        String userId = AuthContext.getUserId(request);
        SeckillGoodsDTO seckillGoodsDTO = promoService.getSeckillGoodsDTO(skuId);
        if(seckillGoodsDTO!=null){
            //判断一下当下的时间是不是在秒杀的时间之内
            Date date = new Date();
            Date startTime = seckillGoodsDTO.getStartTime();
            Date endTime = seckillGoodsDTO.getEndTime();
            if(date.after(startTime)&&date.before(endTime)){
                String encrypted = MD5.encrypt(userId + skuId);
                return Result.ok(encrypted);
            }
        }
        return Result.build(null, ResultCodeEnum.ILLEGAL_REQUEST);
    }
    @GetMapping("/seckill/auth/trade/{skuId}")
    public Result trade(@PathVariable Long skuId, @RequestParam String skuIdStr,HttpServletRequest request){
        //判断是不是同一个md5加密而来的
        String userId = AuthContext.getUserId(request);
        String currentWord = MD5.encrypt(userId + skuId);
        if(!skuIdStr.equals(currentWord)){
         return Result.build(null,SeckillCodeEnum.SECKILL_ILLEGAL);
        }
        //判断库存状态位是不是合法，"1"代表有库存
        String key=skuId.toString();
        Object o = LocalCacheHelper.get(key);
        if(o==null||SeckillGoodsStockStatus.STOCK_NOT_ENOUGH.equals(o)){
            return Result.build(null, SeckillCodeEnum.SECKILL_FINISH);
        }
        //

        //下单
        OrderTradeDTO tradeData = promoService.getTradeData(userId, skuId);
        return Result.ok(tradeData);
    }
    @PostMapping("/seckill/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfoParam orderInfoParam, HttpServletRequest request) {
        // 1. 获取用户id
        String userId = AuthContext.getUserId(request);

        // 2. 校验本地库存状态位
        Long skuId = orderInfoParam.getOrderDetailList().get(0).getSkuId();
        Object stockFlag = LocalCacheHelper.get(skuId.toString());
        if (stockFlag==null||"0".equals(stockFlag)) {
            return Result.build(null, SeckillCodeEnum.SECKILL_FINISH);
        }

        // 3. 校验是否重复下单（redis中）
        RSet<Long> set = redissonClient.getSet(RedisConst.PROMO_USER_ORDERED_FLAG + userId);
        boolean ret = set.tryAdd(skuId);
        if (!ret) {
            //set中已经有值了
            return Result.build(null, SeckillCodeEnum.SECKILL_DUPLICATE_TRADE);     //重复下单
        }

        orderInfoParam.setUserId(Long.valueOf(userId));


        // // 4. 提交秒杀订单
        // boolean submitOrderRet = promoService.submitOrder(orderInfoParam);
        // //失败返回秒杀结束
        // if (!submitOrderRet) {
        //     return Result.build(null, SeckillCodeEnum.SECKILL_FINISH);
        // }
        // // 成功返回下单成功
        // return Result.build(null, SeckillCodeEnum.SECKILL_ORDER_SUCCESS);


        //4.优化
        // 改造： 使用基于RocketMQ的事务消息来控制流程 分布式事务
        promoService.submitOrderInTransaction(orderInfoParam);
        // 如果在这里返回的时候，这个用户抢购商品，库存已经扣减了，但是订单生成异步的，取决于消息什么时候消费成功
        // 所以在此处，只能返回 【抢单成功】， 不能返回下单成功
        return Result.build(null, SeckillCodeEnum.SECKILL_SUCCESS);
    }
    @GetMapping("/seckill/auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId, HttpServletRequest request) {

        // 1. 获取用户id
        String userId = AuthContext.getUserId(request);

        // 2. 检查用户订单的生成情况
        boolean ret = promoService.checkOrder(skuId, userId);
        if (ret) {
            // 如果检查到了订单，返回【下单成功】
            // 一旦返回下单成功，那么前端的定时任务就会结束
            return Result.build(null,SeckillCodeEnum.SECKILL_ORDER_SUCCESS);
        }

        // 如果没有检查到订单，返回 【正在排队中】
        // 如果返回正在排队中，那么前端会再次来检查
        return Result.build(null,SeckillCodeEnum.SECKILL_RUN);

    }
}
