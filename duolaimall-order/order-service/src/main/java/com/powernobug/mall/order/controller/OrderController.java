package com.powernobug.mall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.common.constant.ResultCodeEnum;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.common.util.AuthContext;
import com.powernobug.mall.mq.constant.MqTopicConst;
import com.powernobug.mall.mq.producer.BaseProducer;
import com.powernobug.mall.order.chain.SaveOrderContext;
import com.powernobug.mall.order.chain.handler.*;
import com.powernobug.mall.order.client.CartApiClient;
import com.powernobug.mall.order.client.ProductApiClient;
import com.powernobug.mall.order.client.WareApiClient;
import com.powernobug.mall.order.converter.OrderInfoConverter;
import com.powernobug.mall.order.dto.OrderInfoDTO;
import com.powernobug.mall.order.dto.OrderTradeDTO;
import com.powernobug.mall.order.model.OrderInfo;
import com.powernobug.mall.order.query.OrderDetailParam;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.controller
 * @author: HuangWeiLong
 * @date: 2024/10/11 16:55
 */
@RestController
public class OrderController {
  @Autowired
  OrderService orderService;
  @Autowired
  WareApiClient wareApiClient;
  @Autowired
  ProductApiClient productApiClient;
  @Autowired
  CartApiClient cartApiClient;
  @Autowired
  OrderInfoConverter orderInfoConverter;
  @Autowired
  BaseProducer baseProducer;
  @Autowired
  CheckPriceHandler checkPriceHandler;
  @Autowired
  CheckStockHandler checkStockHandler;
  @Autowired
  SaveOrderHandler saveOrderHandler;
  @Autowired
  DeleteCartHandler deleteCartHandler;
  @Autowired
  SendMessageHandler sendMessageHandler;
  @GetMapping("order/auth/trade")
  public Result orderTrade(HttpServletRequest request){
    String userId = AuthContext.getUserId(request);
    OrderTradeDTO orderTradeDTO=orderService.getTradeInfo(userId);
    return Result.ok(orderTradeDTO);
  }
  @PostMapping("/order/auth/submitOrder")
  public Result submitOrder(@RequestBody OrderInfoParam orderInfo, HttpServletRequest request) {
/*    // 获取到用户Id
    String userId = AuthContext.getUserId(request);
    List<OrderDetailParam> orderDetailList = orderInfo.getOrderDetailList();
    // 1. 校验库存是否足够
    for (OrderDetailParam param : orderDetailList) {
      Long skuId = param.getSkuId();
      String skuName = param.getSkuName();
      Integer skuNum = param.getSkuNum();
      Result result = wareApiClient.hasStock(skuId, skuNum);

      Integer code = ResultCodeEnum.SUCCESS.getCode();
      if(!code.equals(result.getCode())){
       return Result.fail().message(skuName+"库存不足！");
      }
    }

    // 2. 校验商品价格是否产生变动
    for (OrderDetailParam param : orderDetailList) {
      Long skuId = param.getSkuId();
      BigDecimal skuPrice = productApiClient.getSkuPrice(skuId);
      BigDecimal orderPrice = param.getOrderPrice();
      if(!orderPrice.equals(skuPrice)){
        orderService.refreshPrice(skuId,userId);
        return Result.fail().message(param.getSkuName() + "价格发生变动");
      }
    }

    // 3. 保存订单以及订单详情
    OrderInfo convertedOrderInfoParam = orderInfoConverter.convertOrderInfoParam(orderInfo);
    convertedOrderInfoParam.setUserId(Long.valueOf(userId));
    Long orderId = orderService.saveOrderInfo(convertedOrderInfoParam);

    // 4. 删除购物车中已经下单的商品
    List<Long> skuIdList = orderDetailList.stream().map(OrderDetailParam::getSkuId).collect(Collectors.toList());
    cartApiClient.removeCartProductsInOrder(userId,skuIdList);

    // 5. 发送订单超时自动取消的消息
    Boolean ret = baseProducer.sendDelayMessage(MqTopicConst.DELAY_ORDER_TOPIC, orderId, MqTopicConst.DELAY_ORDER_LEVEL);
    // 6. 返回
    if(ret){
      return Result.ok(orderId);
    }
    return Result.fail();*/

    //构造责任链
    checkPriceHandler.setNext(checkStockHandler);
    checkStockHandler.setNext(saveOrderHandler);
    saveOrderHandler.setNext(deleteCartHandler);
    deleteCartHandler.setNext(sendMessageHandler);

    String userId = AuthContext.getUserId(request);
    SaveOrderContext saveOrderContext = new SaveOrderContext();
    saveOrderContext.setOrderInfoParam(orderInfo);
    saveOrderContext.setUserId(userId);

    checkPriceHandler.handle(saveOrderContext);
    return Result.ok(saveOrderContext.getOrderId());
  }

  @GetMapping("/order/auth/{page}/{limit}")
  public Result<IPage<OrderInfoDTO>> index(@PathVariable Long page, @PathVariable Long limit, HttpServletRequest request) {
    // 获取到用户Id
    String userId = AuthContext.getUserId(request);

    Page<OrderInfo> pageParam = new Page<>(page, limit);

    // 获取用户订单的分页列表
    // 注意：
    // 1. 建议使用MybatisPlus的分页查询功能
    // 2. 思考一下哪些订单状态不用在用户页面展示出来？
    // 3. 查询结果需要设置OrderInfoDTO中的orderStatusName(如'未支付'| '已支付' | '已发货')
    IPage<OrderInfoDTO> pageModel = orderService.getPage(pageParam, userId);

    return Result.ok(pageModel);
  }


}
