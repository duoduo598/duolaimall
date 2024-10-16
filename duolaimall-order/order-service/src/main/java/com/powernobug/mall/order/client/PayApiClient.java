package com.powernobug.mall.order.client;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.pay.api.dto.PaymentInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 创建日期: 2023/03/18 21:53
 *
 * @author ciggar
 */
@FeignClient(value = "service-payment")
public interface PayApiClient {

    /**
     * 根据外部订单号 查询支付记录
     */
    @GetMapping("/api/payment/inner/getPaymentInfoByOutTradeNo/{outTradeNo}")
    PaymentInfoDTO getPaymentInfoDTOByOutTradeNo(@PathVariable(value = "outTradeNo") String outTradeNo);

    /**
     * 根据外部订单号 查询支付宝支付状态
     */
    @GetMapping("/api/payment/inner/getAlipayInfo/{outTradeNo}")
    Result getAlipayInfo(@PathVariable(value = "outTradeNo") String outTradeNo);

    /**
     * 关闭支付宝支付记录
     */
    @GetMapping("/api/payment/inner/closeAlipay/{outTradeNo}")
    Result closeAlipay(@PathVariable(value = "outTradeNo") String outTradeNo);

    /**
     * 修改paymentInfo为已关闭
     */
    @GetMapping("/api/payment/inner/closePaymentInfo/{outTradeNo}")
    Result closePaymentInfo(@PathVariable(value = "outTradeNo") String outTradeNo);


}
