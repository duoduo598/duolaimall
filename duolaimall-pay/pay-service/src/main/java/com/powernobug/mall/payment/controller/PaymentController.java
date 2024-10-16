package com.powernobug.mall.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.powernobug.mall.common.constant.RedisConst;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.order.dto.OrderInfoDTO;
import com.powernobug.mall.pay.api.dto.PaymentInfoDTO;
import com.powernobug.mall.payment.alipay.CsmallAlipayConfig;
import com.powernobug.mall.payment.client.OrderApiClient;
import com.powernobug.mall.payment.constant.PaymentType;
import com.powernobug.mall.payment.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.payment.controller
 * @author: HuangWeiLong
 * @date: 2024/10/11 22:13
 */
@RestController
@Slf4j
public class PaymentController {
    @Autowired
    OrderApiClient orderApiClient;
    @Autowired
    PayService payService;
    @Autowired
    CsmallAlipayConfig csmallAlipayConfig;
    @Autowired
    RedissonClient redissonClient;


    // 1. 下单成功，获取订单信息
    @GetMapping("/pay/auth")
    public Result getOrderInfo(Long orderId) {
        OrderInfoDTO orderInfoDTO = orderApiClient.getOrderInfoDTO(orderId);
        return Result.ok(orderInfoDTO);
    }
    @RequestMapping("/pay/alipay/submit/{orderId}")
    public String submitOrder(@PathVariable Long orderId)  {

        String form = payService.createAliPay(orderId);
        return form;
    }

    //接收异步回调的参数，然后进行1.检验签名（支付宝公钥）2.检验业务参数（appid,订单状态,总金额）3.幂等性检验（每次调用结果不变）
    //4.对支付表进行状态的修改；order表的修改；库存的删减
    @PostMapping("pay/alipay/notify")
    public String alipayNotify(@RequestParam Map<String,String> paramsMap) throws AlipayApiException {

        String outTradeNo = paramsMap.get("out_trade_no");
        String notifyId = paramsMap.get("notify_id");
        log.info("【异步通知】来啦，通知的outTradeNo:{}, 通知的notifyId:{}",outTradeNo, notifyId);

        // 1. 验签（防止数据泄露甚至被篡改）
        boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, csmallAlipayConfig.getAlipayPublicKey(), CsmallAlipayConfig.charset, CsmallAlipayConfig.sign_type);
        if (!signVerified) {
            log.info("异步通知验签失败了, 验签参数 paramsMap:{}, charset:{}, signType:{}", JSON.toJSONString(paramsMap),  CsmallAlipayConfig.charset, CsmallAlipayConfig.sign_type);
            return "failure";
        }

        // 2. 验证业务参数
        String tradeNo = paramsMap.get("trade_no");                      // 支付宝为此次支付生成的交易流水号
        String totalAmount = paramsMap.get("total_amount");              // 用户付款总金额
        String appId = paramsMap.get("app_id");                          // 获取应用id
        String tradeStatus = paramsMap.get("trade_status");              // 获取交易状态

        // 验证APPID (当前这个异步通知，是不是我们这个应用程序处理的异步通知)
        if (!csmallAlipayConfig.getAppId().equals(appId)) {
            log.info("appId验证失败, 当前appId:{}, 异步通知中的appId:{}", csmallAlipayConfig.getAppId(), appId);
            return "failure";
        }
        // 验证金额 (看一下异步通知中的金额，和当前这笔支付记录中的总金额是否一致)
        PaymentInfoDTO paymentInfoDTO = payService.queryPaymentInfoByOutTradeNoAndPaymentType(outTradeNo, PaymentType.ALIPAY.name());
        if (paymentInfoDTO == null) {
            log.info("支付表中支付记录不存在, outTradeNo:{}, paymentTypeName:{}", outTradeNo,  PaymentType.ALIPAY.name());
        }
        BigDecimal total = paymentInfoDTO.getTotalAmount();
        if (!new BigDecimal(totalAmount).equals(total)) {
            log.info("金额不一致， total:{}, totalAmount:{}", total, totalAmount);
            return "failure";
        }

        // 交易状态
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            log.info("交易状态不正确, tradeStatus:{}", tradeStatus);
            return "failure";
        }


        // 3. 幂等性的校验
        // 去Redis中打一个标记 (setnx)
        // key: notifyId    value: outTradeNo
        // 如果Redis中存在这个notifyId，证明这个异步通知已经通知过了
        String key = RedisConst.PAY_CALL_BACK_VERFY_PREFIX + notifyId;
        RBucket<String> bucket = redissonClient.getBucket(key);
        boolean ret = bucket.trySet(outTradeNo);//返回false
        if (!ret) {
            log.info("幂等标记存在了，当前通知已经通知过了, notifyId:{}, outTradeNo:{}", notifyId, outTradeNo);
            return "success";
        }
        // 4. 修改支付表的状态  修改订单的状态  扣减库存 (后续执行业务的过程中，如果发生了异常，删除notifyId这个幂等标记)
        Boolean updatePayRet = payService.successPay(outTradeNo, PaymentType.ALIPAY.name(), paramsMap);
        if (!updatePayRet) {
            // 如果失败，删除幂等标记
            bucket.delete();
            return "failure";
        }

        return "success";
    }
}
