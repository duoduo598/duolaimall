package com.powernobug.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.cart.api.dto.CartInfoDTO;
import com.powernobug.mall.common.constant.ResultCodeEnum;
import com.powernobug.mall.common.execption.BusinessException;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.common.util.DateUtil;
import com.powernobug.mall.mq.constant.MqTopicConst;
import com.powernobug.mall.mq.producer.BaseProducer;
import com.powernobug.mall.order.client.CartApiClient;
import com.powernobug.mall.order.client.PayApiClient;
import com.powernobug.mall.order.client.UserApiClient;
import com.powernobug.mall.order.client.WareApiClient;
import com.powernobug.mall.order.constant.OrderStatus;
import com.powernobug.mall.order.constant.OrderType;
import com.powernobug.mall.order.converter.CartInfoConverter;
import com.powernobug.mall.order.converter.OrderDetailConverter;
import com.powernobug.mall.order.converter.OrderInfoConverter;
import com.powernobug.mall.order.dto.OrderDetailDTO;
import com.powernobug.mall.order.dto.OrderInfoDTO;
import com.powernobug.mall.order.dto.OrderTradeDTO;
import com.powernobug.mall.order.mapper.OrderDetailMapper;
import com.powernobug.mall.order.mapper.OrderInfoMapper;
import com.powernobug.mall.order.model.OrderDetail;
import com.powernobug.mall.order.model.OrderInfo;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.order.service.OrderService;
import com.powernobug.mall.pay.api.dto.PaymentInfoDTO;
import com.powernobug.mall.user.dto.UserAddressDTO;
import com.powernobug.mall.ware.api.dto.WareOrderTaskDTO;
import com.powernobug.mall.ware.api.dto.WareSkuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.service.impl
 * @author: HuangWeiLong
 * @date: 2024/10/11 17:07
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    UserApiClient userApiClient;
    @Autowired
    CartApiClient cartApiClient;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    CartInfoConverter cartInfoConverter;
    @Autowired
    OrderInfoConverter orderInfoConverter;
    @Autowired
    OrderDetailConverter orderDetailConverter;
    @Autowired
    WareApiClient wareApiClient;
    @Autowired
    PayApiClient payApiClient;
    @Autowired
    BaseProducer baseProducer;
    @Override
    public OrderTradeDTO getTradeInfo(String userId) {
        OrderTradeDTO orderTradeDTO = new OrderTradeDTO();
        //1.用户地址
        List<UserAddressDTO> userAddressListByUserId = userApiClient.findUserAddressListByUserId(userId);
        orderTradeDTO.setUserAddressList(userAddressListByUserId);
        //2.获取订单详情
        List<CartInfoDTO> cartInfoDTOList = cartApiClient.getCartCheckedList(userId);
        List<OrderDetailDTO> orderDetailDTOS = cartInfoConverter.convertCartInfoDTOToOrderDetailDTOList(cartInfoDTOList);
        orderTradeDTO.setDetailArrayList(orderDetailDTOS);
        //3.总数，总价
        Integer totalNum= 0;
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderDetailDTO orderDetailDTO : orderDetailDTOS) {
            Integer skuNum = orderDetailDTO.getSkuNum();
            BigDecimal orderPrice = orderDetailDTO.getOrderPrice();
            BigDecimal partTotalPrice=orderPrice.multiply(new BigDecimal(skuNum));
            totalAmount=totalAmount.add(partTotalPrice);
            totalNum+=skuNum;
        }
        orderTradeDTO.setTotalNum(totalNum);
        orderTradeDTO.setTotalAmount(totalAmount);
        return orderTradeDTO;
    }
    @Override
    public Boolean checkPrice(Long skuId, BigDecimal skuPrice) {
        return null;
    }

    @Override
    public void refreshPrice(Long skuId, String userId) {
        cartApiClient.refreshCartPrice(userId, skuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrderInfo(OrderInfo orderInfo) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());//订单状态
        orderInfo.setOutTradeNo(uuid);//外部订单号
        orderInfo.sumTotalAmount();//总金额
        orderInfo.setTradeBody(orderInfo.getOrderDetailList().get(0).getSkuName());//订单标题
        Date expireTime= DateUtil.datePlusMinutes(new Date(),2);
        orderInfo.setExpireTime(expireTime);

        int affectedRows = orderInfoMapper.insert(orderInfo);
        if(affectedRows<1){
            throw new BusinessException(ResultCodeEnum.SAVE_ORDER_FAIL);
        }
        Long orderId = orderInfo.getId();
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            orderDetail.setId(null);
            orderDetail.setOrderId(orderId);
            int inserted = orderDetailMapper.insert(orderDetail);
            if(inserted<1){
                throw new BusinessException(ResultCodeEnum.SAVE_ORDER_FAIL);
            }
        }
        return orderId;
    }

    @Override
    public OrderInfoDTO getOrderInfo(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfoConverter.convertOrderInfoToOrderInfoDTO(orderInfo);
    }

    @Override
    public IPage<OrderInfoDTO> getPage(Page<OrderInfo> pageParam, String userId) {
        List<String> list = new ArrayList<>();
        list.add("UNPAID");
        list.add("PAID");
        list.add("WAIT_DELEVER");
        list.add("DELEVERED");
        list.add("CLOSED");
        list.add("COMMENT");
        list.add("PAY_FAIL");
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId,Long.parseLong(userId));
        wrapper.orderByDesc(OrderInfo::getId);
        wrapper.in(OrderInfo::getOrderStatus,list);
        Page<OrderInfo> orderInfoPage = orderInfoMapper.selectPage(pageParam, wrapper);

        //得到新的list
        IPage<OrderInfoDTO> orderInfoDTOIPage = orderInfoConverter.convertOrderInfoPageToOrderInfoDTOPage(orderInfoPage);
        List<OrderInfoDTO> records = orderInfoDTOIPage.getRecords();
        for (OrderInfoDTO record : records) {
            //查找当前商品的详情，进行封装
            LambdaQueryWrapper<OrderDetail> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(OrderDetail::getOrderId,record.getId());
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper1);
            record.setOrderDetailList(orderDetailConverter.convertOrderDetailsToDTOs(orderDetails));
            //设置OrderStatusName---未支付，已支付，已发货
            String statusDescByStatus = OrderStatus.getStatusDescByStatus(record.getOrderStatus());//英文转中文
            record.setOrderStatusName(statusDescByStatus);
        }
        orderInfoDTOIPage.setRecords(records);

        return orderInfoDTOIPage;
    }

    @Override
    public void successPay(Long orderId) {
        //1.更新订单表
        OrderInfoDTO orderInfoDTO = getOrderInfo(orderId);
        OrderInfo info = orderInfoConverter.copyOrderInfo(orderInfoDTO);

        info.setOrderStatus(OrderStatus.PAID.name());


        int affectedRows = orderInfoMapper.updateById(info);
        if(affectedRows<1){
            throw new RuntimeException("订单更新失败");
        }
        //2.更新库存表
        Result result = wareApiClient.decreaseStock(orderId);
        if(!ResultCodeEnum.SUCCESS.getCode().equals(result.getCode())){
            throw new RuntimeException("库存更新异常");
        }

    }

    @Override
    public void successLockStock(String orderId, String taskStatus) {
        // 如果库存扣减成功， 修改订单状态为 【待发货】
        // 如果库存扣减失败，修改订单状态为 【库存扣减异常】

        String orderStatus = "DEDUCTED".equals(taskStatus) ? OrderStatus.WAIT_DELEVER.name() : OrderStatus.STOCK_EXCEPTION.name();

        //不能用new来更新，会使原本订单类型为promoOrder的订单变成默认值的normalOrder
        OrderInfoDTO orderInfoDTO = getOrderInfo(Long.valueOf(orderId));
        OrderInfo orderInfo = orderInfoConverter.copyOrderInfo(orderInfoDTO);

        orderInfo.setOrderStatus(orderStatus);

        int affectedRows = orderInfoMapper.updateById(orderInfo);
        if (affectedRows < 1) {
            throw new RuntimeException("修改订单状态异常");
        }
    }

    /**
     * 支付回调：拆单
     * @param orderId               原订单id
     * @param wareSkuDTOList        仓库商品对象集合
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WareOrderTaskDTO> orderSplit(String orderId, List<WareSkuDTO> wareSkuDTOList) {

        // 1. 查询原订单
        OrderInfoDTO originalOrderInfoDTO = getOrderInfo(Long.valueOf(orderId));
        if (originalOrderInfoDTO == null) {
            return new ArrayList<WareOrderTaskDTO>();
        }

        // 原订单中所有的商品   1001,1002,1003,2001,2002
        List<OrderDetailDTO> originalOrderDetailList = originalOrderInfoDTO.getOrderDetailList();


        // 2. 循环遍历 wareSkuDTOList
        List<WareOrderTaskDTO> wareOrderTaskDTOS = wareSkuDTOList.stream().map(wareSkuDTO -> {
            //获取某个仓库id下的所有商品
            String wareId = wareSkuDTO.getWareId();             // 仓库id
            List<String> skuIds = wareSkuDTO.getSkuIds();       // 当前子订单中的所有商品   1001,1002

            // 3. 创建子订单
            OrderInfo subOrderInfo = orderInfoConverter.copyOrderInfo(originalOrderInfoDTO);

            // 给子订单赋值 订单明细
            List<OrderDetail> subOrderDetailList = originalOrderDetailList.stream().map(orderDetailDTO -> {
                Long skuId = orderDetailDTO.getSkuId();
                if (skuIds.contains(skuId.toString())) {
                    // 转化
                    OrderDetail orderDetail = orderDetailConverter.convertOrderDetailToDTO(orderDetailDTO);
                    orderDetail.setId(null);        // 把子订单的订单明细的id设置为null
                    return orderDetail;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());


            // 给子订单赋值 订单明细
            subOrderInfo.setOrderDetailList(subOrderDetailList);

            // 赋值
            subOrderInfo.setId(null);                                           // 订单id
            subOrderInfo.setParentOrderId(originalOrderInfoDTO.getId());        // 父订单id
            subOrderInfo.sumTotalAmount();                                      // 设置总价格
            subOrderInfo.setOutTradeNo(UUID.randomUUID().toString().replaceAll("-", ""));    // 设置外部订单编号
            subOrderInfo.setTradeBody(subOrderInfo.getOrderDetailList().get(0).getSkuName());       // 设置订单标题

            // 4. 保存子订单
            Long subOrderId = saveOrderInfo(subOrderInfo);


            subOrderInfo.setWareId(wareId);
            // 把子订单转化为库存工作单
            WareOrderTaskDTO wareOrderTaskDTO = orderInfoConverter.convertOrderInfoToWareOrderTaskDTO(subOrderInfo);

            return wareOrderTaskDTO;
        }).collect(Collectors.toList());

        // 5. 修改原订单的状态【已拆分】
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(originalOrderInfoDTO.getId());
        orderInfo.setOrderStatus(OrderStatus.SPLIT.name());
        int affectedRows = orderInfoMapper.updateById(orderInfo);
        if (affectedRows < 1) {
            throw new RuntimeException("修改原订单失败");
        }

        // 6. 返回
        return wareOrderTaskDTOS;
    }

    /**
     * 处理订单超时自动取消
     *
     * 之前的业务逻辑
     *
     *  // 1. 查询订单
     *  // 2. 判断订单状态
     *         // 如果是【未支付】 , 修改订单状态 → 【已取消】
     *         // 如果不是未支付, 啥也不干
     *
     * 之前的业务逻辑不够严谨
     * 举个例子:
     *  比如当前用户在 12:00:00下单，订单超时时间是30min，那么订单服务就会在 12:30:00 收到订单超时自动取消的消息
     *  假如用户在 12:29:59的时候进行支付
     *  那么支付完成之后，支付宝会进行异步回调,理论上来说支付宝马上就会进行异步，也就是给我们的支付服务发请求
     *  但是有的时候可能网络条件不好，假如支付宝第一次异步通知失败了，那么异步通知就会重试，第一次重试在4min之后
     *  所以，我们的支付服务可能收到异步通知时间在 12:33:59 才收到异步通知
     *
     *  但是我们的订单超时自动取消的消息会再12:30:00的时候进行消费,那么此时发现订单的状态依然是未支付，
     *  那么就意味着我们当前的订单超时自动取消会把订单状态修改为【已关闭】
     *
     *  那么对于用户而言，也就是刚刚付款完成，然后查看订单状态，此时看到订单状态就是 【已关闭】，那么就出现了问题
     *
     *
     *  修改的方案：
     *      1. 查询订单
     *      2. 如果订单状态不是【未支付】，说明异步回调回调到了，或者是订单超时自动取消的消息已经消费过了，那么此时 啥也不做
     *      3. 如果是【未支付】 （查询一下用户到底有没有支付）
     *          // 4. 查询支付服务中的支付表
     *                  // 如果不存在记录  说明用户没有点击【扫码支付】, 那么此时 【取消订单】
     *                  // 如果存在记录    说明用户点击了 【扫码支付】
     *                          // 查询支付宝的交易记录
     *                                  // 如果没有查到 (tradeStatus = null) 说明用户虽然点击了扫码支付，但是没有输入用户名和密码， 那么 【关闭订单】 【关闭本地支付记录】
     *                                  // 如果查到的状态 (tradeStatus = WAIT_BUYER_PAY) , 说明用户点击了扫码支付，输入了用户名和密码，但是输入支付密码，确认付款 【关闭订单】【关闭本地支付记录】 【关闭支付宝】
     *                                  // 如果查到的状态 (tradeStatus = TRADE_SUCCESS), 说明用户输入了支付密码，付款成功了, 说明异步回调还在路上，啥也不做
     *                                  // 如果查到的状态 (tradeStatus = TRADE_CLOSED), 说明支付宝那边支付超时了，支付宝主动把这笔支付关闭了, 【关闭订单】【关闭本地支付记录】
     *
     * 上述的方案中， 涉及到的操作有:  PayApiClient
     *      1. 查询订单                       本地查询
     *      2. 查询本地支付表的支付记录         远程调用支付服务的接口
     *      3. 查询支付宝的支付记录            远程调用支付服务的接口
     *      4. 关闭本地订单                   本地操作
     *      5. 关闭本地支付表的支付            远程调用支付服务的接口
     *      6. 关闭支付宝的支付记录            远程调用支付服务的接口
     *
     */
    @Override
    public void execExpiredOrder(Long orderId) {
        //1. 查询订单
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //2. 如果订单状态不是【未支付】，说明异步回调回调到了，或者是订单超时自动取消的消息已经消费过了，那么此时 啥也不做
        //3. 如果是【未支付】 （查询一下用户到底有没有支付）
               // 4. 查询支付服务中的支付表
        String outTradeNo = orderInfo.getOutTradeNo();//uuid
        if(orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.name())){
            PaymentInfoDTO paymentInfoDTOByOutTradeNo = payApiClient.getPaymentInfoDTOByOutTradeNo(outTradeNo);
            //如果不存在记录（用户没有点击扫码支付）
            if(paymentInfoDTOByOutTradeNo==null){
                closeOrder(orderId);
            }
            //如果存在记录（用户点击了扫码支付）
            // 查询支付宝的交易记录
            Result ret = payApiClient.getAlipayInfo(outTradeNo);
            String tradeStatus= (String) ret.getData();

            // 如果没有查到 (tradeStatus = null) 说明用户虽然点击了扫码支付，但是没有输入用户名和密码， 那么 【关闭订单】 【关闭本地支付记录】
            if(tradeStatus==null){
                closeOrder(orderId);
                payApiClient.closePaymentInfo(outTradeNo);
            }
            // 如果查到的状态 (tradeStatus = WAIT_BUYER_PAY) , 说明用户点击了扫码支付，输入了用户名和密码，但是没有输入支付密码，确认付款 【关闭订单】【关闭本地支付记录】 【关闭支付宝】
            else if("WAIT_BUYER_PAY".equals(tradeStatus)){
                closeOrder(orderId);
                payApiClient.closePaymentInfo(outTradeNo);
                payApiClient.closeAlipay(outTradeNo);
            }
            // 如果查到的状态 (tradeStatus = TRADE_SUCCESS), 说明用户输入了支付密码，付款成功了, 说明异步回调还在路上，啥也不做
            // 如果查到的状态 (tradeStatus = TRADE_CLOSED), 说明支付宝那边支付超时了，支付宝主动把这笔支付关闭了, 【关闭订单】【关闭本地支付记录】
            else if("TRADE_CLOSED".equals(tradeStatus)){
                closeOrder(orderId);
                payApiClient.closePaymentInfo(outTradeNo);
            }
        }
    }

    private void closeOrder(Long orderId) {
        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setId(orderId);
        //如果订单类型已经存在，就设置成原来的
        OrderInfoDTO oldOrderInfo = getOrderInfo(orderId);
        String orderType = oldOrderInfo.getOrderType();

        updateOrderInfo.setOrderType(orderType);
        updateOrderInfo.setOrderStatus(OrderStatus.CLOSED.name());
        orderInfoMapper.updateById(updateOrderInfo);
    }

    @Override
    public Long saveSeckillOrder(OrderInfoParam orderInfoParam) {
        // 1. 转化
        OrderInfo orderInfo = orderInfoConverter.convertOrderInfoParam(orderInfoParam);
        // 2. 给对应的成员变量进行赋值
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());        // 设置订单状态
        orderInfo.sumTotalAmount();                                 // 设置订单总金额
        orderInfo.setOutTradeNo(UUID.randomUUID().toString().replaceAll("-",""));   // 设置外部订单编号
        orderInfo.setTradeBody(orderInfo.getOrderDetailList().get(0).getSkuName()); // 设置订单标题
        Date expireTime = DateUtil.datePlusMinutes(new Date(), 2);
        orderInfo.setExpireTime(expireTime);  // 设置订单过期时间

        // 设置订单类型
        orderInfo.setOrderType(OrderType.PROMO_ORDER.name());

        int affectedRows = orderInfoMapper.insert(orderInfo);
        if(affectedRows<1){
            throw new BusinessException(ResultCodeEnum.SAVE_ORDER_FAIL);
        }
        Long orderId = orderInfo.getId();
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            orderDetail.setId(null);
            orderDetail.setOrderId(orderId);
            int inserted = orderDetailMapper.insert(orderDetail);
            if(inserted<1){
                throw new BusinessException(ResultCodeEnum.SAVE_ORDER_FAIL);
            }
        }
        //过期取消
        Boolean ret = baseProducer.sendDelayMessage(MqTopicConst.DELAY_ORDER_TOPIC, orderId, MqTopicConst.DELAY_ORDER_LEVEL);
        if(!ret){
            throw new BusinessException("发送消息失败", ResultCodeEnum.FAIL.getCode());
        }
        return orderId;
    }
}
