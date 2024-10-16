package com.powernobug.mall.payment.controller.inner;

import com.alipay.api.AlipayApiException;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.pay.api.dto.PaymentInfoDTO;
import com.powernobug.mall.payment.constant.PaymentStatus;
import com.powernobug.mall.payment.constant.PaymentType;
import com.powernobug.mall.payment.factory.AlipayPayHelper;
import com.powernobug.mall.payment.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.payment.controller.inner
 * @author: HuangWeiLong
 * @date: 2024/10/15 23:00
 */
@RestController
public class PayApiController {
    @Autowired
    AlipayPayHelper alipayPayHelper;
    @Autowired
    PayService payService;
    /**
     * 根据外部订单号 查询支付记录
     */
    @GetMapping("/api/payment/inner/getPaymentInfoByOutTradeNo/{outTradeNo}")
    public PaymentInfoDTO getPaymentInfoDTOByOutTradeNo(@PathVariable(value = "outTradeNo") String outTradeNo){
        return payService.queryPaymentInfoByOutTradeNoAndPaymentType(outTradeNo, PaymentType.ALIPAY.name());
    }

    /**
     * 根据外部订单号 查询支付宝支付状态
     */
    @GetMapping("/api/payment/inner/getAlipayInfo/{outTradeNo}")
    public Result getAlipayInfo(@PathVariable(value = "outTradeNo") String outTradeNo) throws AlipayApiException {
        String queryPay = alipayPayHelper.queryPay(outTradeNo);
        return Result.ok(queryPay);
    }

    /**
     * 关闭支付宝支付记录
     */
    @GetMapping("/api/payment/inner/closeAlipay/{outTradeNo}")
    public Result closeAlipay(@PathVariable(value = "outTradeNo") String outTradeNo) throws AlipayApiException {
        String ret = alipayPayHelper.closePay(outTradeNo);
        if(Objects.equals(ret, "false")){
            return null;
        }
        return Result.ok();
    }

    /**
     * 修改paymentInfo为已关闭
     */
    @GetMapping("/api/payment/inner/closePaymentInfo/{outTradeNo}")
    public Result closePaymentInfo(@PathVariable(value = "outTradeNo") String outTradeNo){
        payService.updatePaymentStatus(outTradeNo,PaymentType.ALIPAY.name(), PaymentStatus.CLOSED);
        return Result.ok();
    }
}
