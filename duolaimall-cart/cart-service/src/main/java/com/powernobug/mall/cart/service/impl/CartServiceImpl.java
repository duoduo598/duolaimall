package com.powernobug.mall.cart.service.impl;

import com.powernobug.mall.cart.api.dto.CartInfoDTO;
import com.powernobug.mall.cart.client.ProductApiClient;
import com.powernobug.mall.cart.converter.SkuInfoConverter;
import com.powernobug.mall.cart.service.CartService;
import com.powernobug.mall.common.constant.RedisConst;
import com.powernobug.mall.product.dto.SkuInfoDTO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.cart.service.impl
 * @author: HuangWeiLong
 * @date: 2024/10/9 15:21
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    ProductApiClient productApiClient;
    @Autowired
    SkuInfoConverter skuInfoConverter;

    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        //1.获取购物车
        String redisKey= RedisConst.USER_CART_KEY_SUFFIX+userId;
        RMap<Long, CartInfoDTO> map = redissonClient.getMap(redisKey);
        boolean containsKey = map.containsKey(skuId);
        //2.看看是否已经有了，有了就更新数量
        if(containsKey){
            CartInfoDTO cartInfoDTO = map.get(skuId);
            //在购物车中该商品的数量至少为1
            if(cartInfoDTO.getSkuNum()>0){
                cartInfoDTO.setSkuNum(cartInfoDTO.getSkuNum()+skuNum);
                cartInfoDTO.setUpdateTime(new Date());
                map.put(skuId,cartInfoDTO);
            }else {
                //不大于0，增加删除该商品
                map.remove(skuId);
            }
        }
        //3.没有就直接将其加入购物车
        else {
            SkuInfoDTO skuInfo = productApiClient.getSkuInfo(skuId);
            CartInfoDTO infoDTO = skuInfoConverter.skuInfoToCartInfo(skuInfo, skuNum, skuId, userId);
            map.put(skuId, infoDTO);
        }
    }

    @Override
    public List<CartInfoDTO> getCartList(String userId, String userTempId) {
        //登录用户的cart
        String userKey=RedisConst.USER_CART_KEY_SUFFIX+userId;
        RMap<Long, CartInfoDTO> cartMap = redissonClient.getMap(userKey);
        //未登录用户的临时cart
        String tempUserKey=RedisConst.USER_CART_KEY_SUFFIX+userTempId;
        RMap<Long, CartInfoDTO> tempCartMap = redissonClient.getMap(tempUserKey);

        //1.如果未登录，返回userTempId的购物车
        if(StringUtils.isBlank(userId)){
            List<CartInfoDTO> cartInfoDTOS = tempCartMap.readAllValues().stream().collect(Collectors.toList());
            //排序
            cartInfoDTOS.sort((o1, o2) -> {
                Date updateTime1 = o1.getUpdateTime();
                Date updateTime2 = o2.getUpdateTime();
                return updateTime2.compareTo(updateTime1);
            });
            return cartInfoDTOS;
        }
        //2.如果已经登录
        //看看tempcart是否为空，为空不考虑
        if(CollectionUtils.isEmpty(tempCartMap)){
            List<CartInfoDTO> cartInfoDTOS = cartMap.readAllValues().stream().collect(Collectors.toList());
            //排序
            cartInfoDTOS.sort((o1, o2) -> {
                Date updateTime1 = o1.getUpdateTime();
                Date updateTime2 = o2.getUpdateTime();
                return updateTime2.compareTo(updateTime1);
            });
            return cartInfoDTOS;
        }
        //不为空，进行合并
        List<CartInfoDTO> cartList = merge(cartMap, tempCartMap,userId);
        //3.删除临时购物车，返回合并后的结果
        tempCartMap.delete();
        return cartList;
    }

    private List<CartInfoDTO> merge(RMap<Long, CartInfoDTO> cartMap, RMap<Long, CartInfoDTO> tempCartMap,String userId) {
        //取出临时购物车的商品对象
        List<CartInfoDTO> tempcartInfoDTOS = tempCartMap.readAllValues().stream().collect(Collectors.toList());
        //遍历：有，更新数量；没有，直接增加对象
        for (CartInfoDTO tempcartInfoDTO : tempcartInfoDTOS) {
            Long skuId = tempcartInfoDTO.getSkuId();
            Integer skuNum = tempcartInfoDTO.getSkuNum();
            if(cartMap.containsKey(skuId)){
                CartInfoDTO cartInfoDTO = cartMap.get(skuId);

                cartInfoDTO.setSkuNum(cartInfoDTO.getSkuNum()+skuNum);
                cartInfoDTO.setUpdateTime(new Date());
                cartMap.put(skuId,cartInfoDTO);
            }else {
                SkuInfoDTO skuInfo = productApiClient.getSkuInfo(skuId);
                CartInfoDTO cartInfoDTO = skuInfoConverter.skuInfoToCartInfo(skuInfo, skuNum, skuId, userId);
                cartMap.put(skuId,cartInfoDTO);
            }
        }
        List<CartInfoDTO> cartInfoDTOList = cartMap.readAllValues().stream().collect(Collectors.toList());
        //排序
        cartInfoDTOList.sort((o1, o2) -> {
            Date updateTime1 = o1.getUpdateTime();
            Date updateTime2 = o2.getUpdateTime();
            return updateTime2.compareTo(updateTime1);
        });
        return cartInfoDTOList;
    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        String currentCart=RedisConst.USER_CART_KEY_SUFFIX+userId;
        RMap<Long, CartInfoDTO> cartMap = redissonClient.getMap(currentCart);
        CartInfoDTO cartInfoDTO = cartMap.get(skuId);
        if(cartInfoDTO!=null){
            cartInfoDTO.setIsChecked(isChecked);
            cartMap.put(skuId,cartInfoDTO);
        }
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        String currentKey=RedisConst.USER_CART_KEY_SUFFIX+userId;
        RMap<Long, CartInfoDTO> map = redissonClient.getMap(currentKey);
        map.remove(skuId);
    }

    @Override
    public void deleteChecked(String userId) {
        String currentCartKey=RedisConst.USER_CART_KEY_SUFFIX+userId;
        RMap<Long, CartInfoDTO> map = redissonClient.getMap(currentCartKey);
        List<CartInfoDTO> cartInfoDTOS = map.readAllValues().stream().filter(cartInfoDTO -> cartInfoDTO.getIsChecked() == 1).collect(Collectors.toList());
        for (CartInfoDTO cartInfoDTO : cartInfoDTOS) {
            Long skuId = cartInfoDTO.getSkuId();
            map.remove(skuId);
        }
    }

    @Override
    public List<CartInfoDTO> getCartCheckedList(String userId) {
        List<CartInfoDTO> cartList = getCartList(userId, null);
        List<CartInfoDTO> checkedCartInfo = cartList.stream().filter(cartInfoDTO -> cartInfoDTO.getIsChecked() == 1).collect(Collectors.toList());
        return checkedCartInfo;
    }

    @Override
    public void delete(String userId, List<Long> skuIds) {
        String key=RedisConst.USER_CART_KEY_SUFFIX+userId;
        RMap<Long, CartInfoDTO> map = redissonClient.getMap(key);
        for (Long skuId : skuIds) {
            map.remove(skuId);
        }
    }

    @Override
    public void refreshCartPrice(String userId, Long skuId) {
        String key=RedisConst.USER_CART_KEY_SUFFIX+userId;
        RMap<Long, CartInfoDTO> map = redissonClient.getMap(key);
        BigDecimal skuPrice = productApiClient.getSkuPrice(skuId);
        CartInfoDTO cartInfoDTO = map.get(skuId);
        cartInfoDTO.setCartPrice(skuPrice);
        cartInfoDTO.setSkuPrice(skuPrice);
        map.put(skuId,cartInfoDTO);
    }
}
