package com.powernobug.mall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernobug.mall.user.consts.UserConstants;
import com.powernobug.mall.user.dto.UserLoginDTO;
import com.powernobug.mall.user.dto.UserLoginInfoDTO;
import com.powernobug.mall.user.mapper.UserInfoMapper;
import com.powernobug.mall.user.model.UserInfo;
import com.powernobug.mall.user.query.UserInfoParam;
import com.powernobug.mall.user.service.UserService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.user.service.impl
 * @author: HuangWeiLong
 * @date: 2024/10/8 17:17
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    RedissonClient redissonClient;
    @Override
    public UserLoginDTO login(UserInfoParam userInfo, String ip, String token) {
        String loginName = userInfo.getLoginName();
        String passwd = userInfo.getPasswd();
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());

        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.eq(UserInfo::getLoginName, loginName);
        userInfoLambdaQueryWrapper.eq(UserInfo::getPasswd,newPasswd);
        UserInfo info = userInfoMapper.selectOne(userInfoLambdaQueryWrapper);
        if(info==null){
            return null;
        }
        //
        UserLoginInfoDTO userLoginInfoDTO = new UserLoginInfoDTO();
        userLoginInfoDTO.setUserId(info.getId().toString());
        userLoginInfoDTO.setIp(ip);


        String key= UserConstants.USER_LOGIN_KEY_PREFIX+token;
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(userLoginInfoDTO,1, TimeUnit.DAYS);

        return new UserLoginDTO(info.getNickName(),token);
    }
}
