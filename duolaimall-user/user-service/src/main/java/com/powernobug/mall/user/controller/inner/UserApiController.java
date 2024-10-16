package com.powernobug.mall.user.controller.inner;

import com.powernobug.mall.user.converter.UserAddressConverter;
import com.powernobug.mall.user.dto.UserAddressDTO;
import com.powernobug.mall.user.model.UserAddress;
import com.powernobug.mall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.user.controller.inner
 * @author: HuangWeiLong
 * @date: 2024/10/11 17:36
 */
@RestController
public class UserApiController {
    @Autowired
    UserAddressService userAddressService;
    @Autowired
    UserAddressConverter userAddressConverter;
    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    public List<UserAddressDTO> findUserAddressListByUserId(@PathVariable("userId") String userId){
        List<UserAddress> userAddress = userAddressService.findUserAddressListByUserId(userId);
        return userAddressConverter.userAddressPOs2DTOs(userAddress);
    }
}
