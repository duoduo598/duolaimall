package com.powernobug.mall.user.converter;


import com.powernobug.mall.user.dto.UserAddressDTO;
import com.powernobug.mall.user.model.UserAddress;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserAddressConverter {

    UserAddressDTO userAddressPO2DTO(UserAddress userAddress);
    List<UserAddressDTO> userAddressPOs2DTOs(List<UserAddress> userAddresses);
}
