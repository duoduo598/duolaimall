package com.powernobug.mall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernobug.mall.common.constant.ResultCodeEnum;
import com.powernobug.mall.common.execption.BusinessException;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.order.dto.OrderInfoDTO;
import com.powernobug.mall.pay.api.dto.PaymentInfoDTO;
import com.powernobug.mall.payment.factory.AlipayPayHelper;
import com.powernobug.mall.payment.client.OrderApiClient;
import com.powernobug.mall.payment.constant.PaymentStatus;
import com.powernobug.mall.payment.constant.PaymentType;
import com.powernobug.mall.payment.converter.PaymentInfoConverter;
import com.powernobug.mall.payment.mapper.PaymentInfoMapper;
import com.powernobug.mall.payment.model.PaymentInfo;
import com.powernobug.mall.payment.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.payment.service.impl
 * @author: HuangWeiLong
 * @date: 2024/10/14 21:45
 */
@Service
public class PayServiceImpl implements PayService {
    @Autowired
    AlipayPayHelper payHelper;
    @Autowired
    OrderApiClient orderApiClient;
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    PaymentInfoConverter paymentInfoConverter;
    @Override
    public String createAliPay(Long orderId) {
        // 在上面这个支付的方法中，需要做的事情有如下几个

        // 1. 校验支付对应的订单状态是否为未支付，如果是已支付或已关闭，则直接返回
        OrderInfoDTO orderInfoDTO = orderApiClient.getOrderInfoDTO(orderId);
        if(orderInfoDTO.getOrderStatus().equals("PAID")||orderInfoDTO.getOrderStatus().equals("CLOSED")){
            return null;
        }
        // 2. 保存支付记录到支付表
        savePaymentInfo(orderInfoDTO, PaymentType.ALIPAY.name());
        // 3. 调用支付宝SDK，生成支付表单（支付表单实际上就是一个支付页面，是一个html的字符串）
        String page = null;
        try {
            page = payHelper.getPage(orderInfoDTO);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BusinessException(ResultCodeEnum.ALIPAY_FAIL);
        }
        // 4. 返回支付表单
        return page;
    }

    @Override
    public void savePaymentInfo(OrderInfoDTO orderInfo, String paymentTypeName) {
        PaymentInfo paymentInfo = paymentInfoConverter.contvertOrderInfoDTO2PaymentInfo(orderInfo);
        paymentInfo.setPaymentStatus(orderInfo.getOrderStatus());
        paymentInfo.setPaymentType(paymentTypeName);
        paymentInfo.setId(null);

        int affectedRows = paymentInfoMapper.insert(paymentInfo);
        if(affectedRows<1){
            throw new BusinessException(ResultCodeEnum.FAIL);
        }
    }

    @Override
    public PaymentInfoDTO queryPaymentInfoByOutTradeNoAndPaymentType(String outTradeNo, String payTypeName) {
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOutTradeNo,outTradeNo);
        wrapper.eq(PaymentInfo::getPaymentType,payTypeName);
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(wrapper);

        return paymentInfoConverter.convertPaymentInfoToDTO(paymentInfo);
    }

    @Override
    public Boolean successPay(String outTradeNo, String name, Map<String, String> paramsMap) {
        try {
            //1.修改支付状态（支付表）未支付----支付
            PaymentInfoDTO paymentInfoDTO = queryPaymentInfoByOutTradeNoAndPaymentType(outTradeNo, name);
            PaymentInfo paymentInfo = paymentInfoConverter.convertPaymentInfoFromDTO(paymentInfoDTO);

            paymentInfo.setCallbackContent(JSON.toJSONString(paramsMap));
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
            paymentInfo.setTradeNo(outTradeNo);

            int affectedRows = paymentInfoMapper.updateById(paymentInfo);
            if(affectedRows<1){
                throw new RuntimeException("更新支付表信息失败");
            }
            //2.修改订单状态
            Result result = orderApiClient.successPay(paymentInfo.getOrderId());
            if(!ResultCodeEnum.SUCCESS.getCode().equals(result.getCode())){
                throw new RuntimeException("更新订单表信息失败");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void updatePaymentStatus(String outTradeNo, String name, PaymentStatus paymentStatus) {
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOutTradeNo,outTradeNo);
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(wrapper);
        if(paymentInfo==null){
            throw new RuntimeException("查询支付表信息失败");
        }
        paymentInfo.setPaymentType(name);
        paymentInfo.setPaymentStatus(paymentStatus.name());
        int affectedRows = paymentInfoMapper.update(paymentInfo, wrapper);
        if(affectedRows<1){
            throw new RuntimeException("更新支付表信息异常");
        }
    }
}
