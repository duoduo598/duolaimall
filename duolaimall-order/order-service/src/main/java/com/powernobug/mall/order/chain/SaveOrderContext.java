package com.powernobug.mall.order.chain;

import com.powernobug.mall.order.query.OrderInfoParam;
import lombok.Data;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.chain
 * @author: HuangWeiLong
 * @date: 2024/10/16 21:15
 */
@Data
public class SaveOrderContext {
    OrderInfoParam orderInfoParam;
    String userId;
    Long orderId;
}
