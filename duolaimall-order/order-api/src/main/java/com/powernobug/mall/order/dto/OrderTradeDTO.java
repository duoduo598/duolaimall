package com.powernobug.mall.order.dto;

import com.powernobug.mall.user.dto.UserAddressDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建日期: 2023/03/15 15:09
 */
@Data
public class OrderTradeDTO {

    // TODO 修改 userAddressList
    List<UserAddressDTO> userAddressList;
    List<OrderDetailDTO> detailArrayList;
    Integer totalNum;
    BigDecimal totalAmount;
}
