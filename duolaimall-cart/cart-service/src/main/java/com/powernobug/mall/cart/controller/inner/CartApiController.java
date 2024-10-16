package com.powernobug.mall.cart.controller.inner;

import com.powernobug.mall.cart.api.dto.CartInfoDTO;
import com.powernobug.mall.cart.service.CartService;
import com.powernobug.mall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.cart.controller.inner
 * @author: HuangWeiLong
 * @date: 2024/10/11 17:45
 */
@RestController
public class CartApiController {
    @Autowired
    CartService cartService;

    // 下单的时候 查询购物车中所有被选中的商品
    @GetMapping("/api/cart/inner/getCartCheckedList/{userId}")
    public List<CartInfoDTO> getCartCheckedList(@PathVariable(value = "userId") String userId){
        return cartService.getCartCheckedList(userId);
    }

    // 更新用户购物车中商品价格
    @GetMapping("/api/cart/inner/refresh/{userId}/{skuId}")
    public Result refreshCartPrice(@PathVariable(value = "userId") String userId, @PathVariable(value = "skuId") Long skuId){
        cartService.refreshCartPrice(userId,skuId);
        return Result.ok();
    }

    @PutMapping("/api/cart/inner/delete/order/cart/{userId}")
    public Result removeCartProductsInOrder(@PathVariable("userId") String userId, @RequestBody List<Long> skuIds){
        cartService.delete(userId,skuIds);
        return Result.ok();
    }
}
