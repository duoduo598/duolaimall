package com.powernobug.mall.user.controller;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.common.util.IpUtil;
import com.powernobug.mall.user.consts.UserCodeEnum;
import com.powernobug.mall.user.consts.UserConstants;
import com.powernobug.mall.user.dto.UserLoginDTO;
import com.powernobug.mall.user.query.UserInfoParam;
import com.powernobug.mall.user.service.UserService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.user.controller
 * @author: HuangWeiLong
 * @date: 2024/10/8 16:49
 */
@RestController
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    RedissonClient redissonClient;
    @PostMapping("/user/login")
    public Result login(@RequestBody UserInfoParam userInfoParam, HttpServletRequest request){
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        String ipAddress = IpUtil.getIpAddress(request);
        UserLoginDTO userLoginDTO = userService.login(userInfoParam, ipAddress, token);
        if(userLoginDTO!=null){
            return Result.ok(userLoginDTO);
        }
        return Result.build(null, UserCodeEnum.USER_LOGIN_CHECK_FAIL);
    }
    @GetMapping("/user/logout")
    public Result logout(HttpServletRequest request){
        RBucket<String> token = redissonClient.getBucket(UserConstants.USER_LOGIN_KEY_PREFIX + request.getHeader("token"));
        token.delete();
        return Result.ok();
    }
}
