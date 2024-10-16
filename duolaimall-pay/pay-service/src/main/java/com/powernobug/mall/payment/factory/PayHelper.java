package com.powernobug.mall.payment.factory;

import com.alipay.api.AlipayApiException;
import com.powernobug.mall.order.dto.OrderInfoDTO;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.payment.factory
 * @author: HuangWeiLong
 * @date: 2024/10/15 17:35
 */
public interface PayHelper {
    String getPage(OrderInfoDTO orderInfoDTO)  throws AlipayApiException;
}
