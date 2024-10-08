package com.powernobug.mall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.powernobug.mall.common.constant.ResultCodeEnum;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.common.util.IpUtil;
import com.powernobug.mall.user.consts.UserConstants;
import com.powernobug.mall.user.dto.UserLoginInfoDTO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.gateway.filter
 * @author: HuangWeiLong
 * @date: 2024/10/8 19:52
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Value("${authUrls.url}")
    String authUrl;
    @Autowired
    RedissonClient redissonClient;
    AntPathMatcher antPathMatcher = new AntPathMatcher();
    /*
    * 三种可能：未登录：null
    *         ip异常："-1"
    *         正常：userId
    * */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();
        //是否需要登录
        boolean isNeeded=NeedLogin(path);

        //是否已经登录（已经登录的返回userId）
        String isLogin=IsLogin(request);


        //ip异常-----拦截
        if("-1".equals(isLogin)){
            return out(response,ResultCodeEnum.ILLEGAL_REQUEST);
        }

        //需要登录，但未登录------拦截
        if(isNeeded&&StringUtils.isEmpty(isLogin)){
            return out(response,ResultCodeEnum.LOGIN_AUTH);
        }

        //需要登录已登录|不需要登录未登录|不需要登录已登录-----放行
        ServerHttpRequest.Builder newrequest = request.mutate();
        if (StringUtils.isNotEmpty(isLogin)){
            newrequest.header("userId",isLogin);
        }

        //临时的UserTempId存一下
        String userTempId = request.getHeaders().getFirst("userTempId");
        if(StringUtils.isEmpty(userTempId)){
            HttpCookie userTempId1 = request.getCookies().getFirst("userTempId");
            if(userTempId1!=null){
                userTempId=userTempId1.getValue();
            }
        }
        if (StringUtils.isNotEmpty(userTempId)){
            newrequest.header("userTempId",userTempId);
        }

        //放行新请求构建的对象
        ServerWebExchange.Builder newexchange = exchange.mutate().request(newrequest.build());
        return chain.filter(newexchange.build());
    }

    private boolean NeedLogin(String path) {
        return antPathMatcher.match(authUrl, path);
    }

    private String IsLogin(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst("token");
        if(StringUtils.isEmpty(token)){
            HttpCookie token1 = request.getCookies().getFirst("token");
            if(token1!=null){
                token = token1.getValue();
            }
        }
        //1.用户未登录或者登录已经过期了
        if(StringUtils.isEmpty(token)){
            return null;
        }

        String key= UserConstants.USER_LOGIN_KEY_PREFIX+token;
        RBucket<UserLoginInfoDTO> bucket = redissonClient.getBucket(key);
        UserLoginInfoDTO userLoginInfoDTO = bucket.get();
        //没查到，过期
        if (userLoginInfoDTO == null) {
            return null;
        }
        //2.ip异常
        String ip = userLoginInfoDTO.getIp();
        String userId = userLoginInfoDTO.getUserId();

        String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
        if(!ip.equals(gatwayIpAddress)){
            return "-1";
        }
        //3.正常返回
        return userId;
    }
    // 接口鉴权失败返回数据
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // 返回用户没有权限登录
        Result<Object> result = Result.build(null, resultCodeEnum);
        // 将result对象转化为json字符串，并将字符串转化为字节数据
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        // 封装一个字节数据为一个DataBuffer，消息体数据
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 输出到页面
        return response.writeWith(Mono.just(wrap));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
