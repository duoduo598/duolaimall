package com.powernobug.mall.order.converter;

import com.powernobug.mall.order.dto.OrderDetailDTO;
import com.powernobug.mall.order.model.OrderDetail;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 创建日期: 2023/03/16 17:37
 *
 * @author ciggar
 */
@Mapper(componentModel = "spring")
public interface OrderDetailConverter {

    OrderDetailDTO convertOrderDetailToDTO(OrderDetail orderDetail);
    List<OrderDetailDTO> convertOrderDetailsToDTOs(List<OrderDetail> orderDetails);

    OrderDetail convertOrderDetailToDTO(OrderDetailDTO detailDTO);
}
