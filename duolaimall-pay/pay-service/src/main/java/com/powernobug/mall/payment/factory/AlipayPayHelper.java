package com.powernobug.mall.payment.factory;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.powernobug.mall.common.constant.ResultCodeEnum;
import com.powernobug.mall.common.execption.BusinessException;
import com.powernobug.mall.common.util.DateUtil;
import com.powernobug.mall.order.dto.OrderInfoDTO;
import com.powernobug.mall.payment.alipay.CsmallAlipayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@Component
public class AlipayPayHelper implements PayHelper{

    @Autowired
    AlipayClient alipayClient;
    @Autowired
    CsmallAlipayConfig csmallAlipayConfig;

    /**
     * 1. 向支付宝发请求，获取支付页面
     */
    public String getPage(OrderInfoDTO orderInfoDTO) throws AlipayApiException {

        // 1. 构造请求对象
        AlipayTradePagePayRequest pagePayRequest = new AlipayTradePagePayRequest();

        // 2. 创建请求体对象
        AlipayTradePagePayModel pagePayModel = new AlipayTradePagePayModel();

        // 3. 填充请求参数
        // outTradeNo
        pagePayModel.setOutTradeNo(orderInfoDTO.getOutTradeNo());
        // subject
        pagePayModel.setSubject(orderInfoDTO.getTradeBody());
        // totalAmount
        pagePayModel.setTotalAmount(orderInfoDTO.getTotalAmount().toString());
        // productCode
        pagePayModel.setProductCode("FAST_INSTANT_TRADE_PAY");
        // 过期时间
        String date = DateUtil.formatDate2(orderInfoDTO.getExpireTime());
        pagePayModel.setTimeExpire(date);

        // 同步回调地址
        pagePayRequest.setReturnUrl("http://localhost:8000/#/pay/success");
        // 异步回调地址 把异步回调跑通（能够接收到异步通知）
        pagePayRequest.setNotifyUrl("http://powernobug.natapp1.cc/pay/alipay/notify");


        // 4. 把请求体对象放到请求对象中
        pagePayRequest.setBizModel(pagePayModel);

        // 5. 发起请求
        AlipayTradePagePayResponse response = alipayClient.pageExecute(pagePayRequest);

        // 6. 解析结果、并返回结果
        if (!response.isSuccess()) {
            throw new BusinessException(ResultCodeEnum.FAIL);
        }

        return response.getBody();  // 支付页面

    }

    //返回当前交易状态
    public String queryPay(@PathVariable String outTradeNo) throws AlipayApiException {

        // 1. 构建请求参数
        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();


        // 2. 构建请求体对象
        AlipayTradeQueryModel queryModel = new AlipayTradeQueryModel();
        queryModel.setOutTradeNo(outTradeNo);                                  // 传入交易流水号


        // 3. 把请求体对象放入请求对象中
        queryRequest.setBizModel(queryModel);

        // 4. 调用API，发起请求
        AlipayTradeQueryResponse queryResponse = alipayClient.execute(queryRequest);

        // 5. 获取响应的结果
        String tradeStatus = queryResponse.getTradeStatus();
        String code = queryResponse.getCode();
        String msg = queryResponse.getMsg();
        String subCode = queryResponse.getSubCode();
        String subMsg = queryResponse.getSubMsg();

        String totalAmount = queryResponse.getTotalAmount();

        System.out.println("tradeStatus = " + tradeStatus);
        System.out.println("code = " + code);
        System.out.println("msg = " + msg);
        System.out.println("subCode = " + subCode);
        System.out.println("subMsg = " + subMsg);
        System.out.println("totalAmount = " + totalAmount);


        return tradeStatus;

    }

    //返回一个true或者false
    public String closePay(@PathVariable String outTradeNo) throws AlipayApiException {

        // 1. 构建关闭交易的请求
        AlipayTradeCloseRequest closeRequest = new AlipayTradeCloseRequest();

        // 2. 创建请求体对象
        AlipayTradeCloseModel closeModel = new AlipayTradeCloseModel();

        // 填充请求参数
        closeModel.setOutTradeNo(outTradeNo);


        // 3. 把请求体对象放入请求对象中
        closeRequest.setBizModel(closeModel);


        // 4. 发起请求
        AlipayTradeCloseResponse closeResponse = alipayClient.execute(closeRequest);

        // 5. 获取结果
        boolean success = closeResponse.isSuccess();


        return success +"";
    }


}
