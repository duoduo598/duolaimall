package com.powernobug.mall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernobug.mall.user.mapper.UserAddressMapper;
import com.powernobug.mall.user.model.UserAddress;
import com.powernobug.mall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.user.service.impl
 * @author: HuangWeiLong
 * @date: 2024/10/11 17:38
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Autowired
    UserAddressMapper userAddressMapper;
    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, Long.parseLong(userId));
        return userAddressMapper.selectList(wrapper);
    }
}
