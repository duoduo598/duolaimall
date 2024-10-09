package com.powernobug.mall.cart.controller;

import com.powernobug.mall.cart.api.dto.CartInfoDTO;
import com.powernobug.mall.cart.service.CartService;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.common.util.AuthContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.cart.controller
 * @author: HuangWeiLong
 * @date: 2024/10/9 15:06
 */
@RestController
public class CartController {
    @Autowired
    CartService cartService;
    @GetMapping("/cart/add/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum, HttpServletRequest request){
        String userId = AuthContext.getUserId(request);
        if(StringUtils.isBlank(userId)){
            userId = AuthContext.getUserTempId(request);
        }
        cartService.addToCart(skuId,userId,skuNum);
        return Result.ok();
    }
    @GetMapping("/cart")
    public Result getCartList(HttpServletRequest request){
        String userId = AuthContext.getUserId(request);
        String userTempId = AuthContext.getUserTempId(request);

        List<CartInfoDTO> cartList = cartService.getCartList(userId, userTempId);
        return Result.ok(cartList);
    }
    @PutMapping("cart/check/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId, @PathVariable Integer isChecked,
                            HttpServletRequest request)
    {
        String userId = AuthContext.getUserId(request);
        if(StringUtils.isBlank(userId)){
            userId=AuthContext.getUserTempId(request);
        }
        // 修改指定用户购物车中指定商品的 isChecked状态
        cartService.checkCart(userId,isChecked,skuId);
        return Result.ok();
    }
    @DeleteMapping("cart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,HttpServletRequest request){
        String userId = AuthContext.getUserId(request);
        if(StringUtils.isBlank(userId)){
            userId=AuthContext.getUserTempId(request);
        }
        cartService.deleteCart(skuId,userId);
        return Result.ok();
    }
    @DeleteMapping("/cart/checked")
    public Result deleteChecked(HttpServletRequest request) {

        // 注意在获取用户购物车数据时，使用和添加购物车时的key相同
        // 根据userId先获取购物车数据

        String userId = AuthContext.getUserId(request);
        if(StringUtils.isBlank(userId)){
            userId=AuthContext.getUserTempId(request);
        }
        // 遍历购物车中的购物车商品数据，删除用户购物车中所有已经被选中的商品
        cartService.deleteChecked(userId);
        return Result.ok();
    }
}
