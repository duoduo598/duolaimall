package com.powernobug.mall.payment.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.payment.factory
 * @author: HuangWeiLong
 * @date: 2024/10/15 17:38
 */
@Component
public class AlipayFactory implements PayFactory{
    @Autowired
    AlipayPayHelper alipayPayHelper;
    @Override
    public PayHelper getPayHelper() {
        return alipayPayHelper;
    }
}
