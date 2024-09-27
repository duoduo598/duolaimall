package com.powernobug.mall.product.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.cache
 * @author: HuangWeiLong
 * @date: 2024/9/27 16:36
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCache {
    String prefix();
}
