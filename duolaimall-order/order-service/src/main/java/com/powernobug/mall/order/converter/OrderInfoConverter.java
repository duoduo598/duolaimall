package com.powernobug.mall.order.converter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.order.dto.OrderInfoDTO;
import com.powernobug.mall.order.model.OrderDetail;
import com.powernobug.mall.order.model.OrderInfo;
import com.powernobug.mall.order.query.OrderDetailParam;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.ware.api.dto.WareOrderTaskDTO;
import com.powernobug.mall.ware.api.dto.WareOrderTaskDetailDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderInfoConverter {

    OrderInfoDTO convertOrderInfoToOrderInfoDTO(OrderInfo orderInfo);

    OrderInfo convertOrderInfoParam(OrderInfoParam orderInfoParam);

    OrderDetail convertOrderDetailParam(OrderDetailParam orderDetailParam);

    @Mapping(source = "id",target = "orderId")
    @Mapping(source = "tradeBody",target = "orderBody" )
    @Mapping(source = "orderDetailList",target = "details")
    WareOrderTaskDTO convertOrderInfoToWareOrderTaskDTO(OrderInfo orderInfo);

    WareOrderTaskDetailDTO convertDetail(OrderDetail orderDetail);

    OrderInfo copyOrderInfo(OrderInfoDTO orderInfo);

    Page<OrderInfoDTO> convertOrderInfoPageToOrderInfoDTOPage(Page<OrderInfo> page);

}
