package com.powernobug.mall.user.service;


import com.powernobug.mall.user.dto.UserLoginDTO;
import com.powernobug.mall.user.query.UserInfoParam;

public interface UserService {

    /**
     * 登录方法
     */
    UserLoginDTO login(UserInfoParam userInfo, String ip, String token);

}
