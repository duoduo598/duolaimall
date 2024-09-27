package com.powernobug.mall.product.cache;

import com.powernobug.mall.common.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.cache
 * @author: HuangWeiLong
 * @date: 2024/9/27 16:33
 */
@Aspect
@Component
public class RedisCacheAspect {
    @Autowired
    RedissonClient redissonClient;
    @Around("@annotation(com.powernobug.mall.product.cache.RedisCache)")
    public Object redisCache(ProceedingJoinPoint proceedingJoinPoint){
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();

        RedisCache redisCache = method.getAnnotation(RedisCache.class);
        String prefix = redisCache.prefix();

        Object[] args = proceedingJoinPoint.getArgs();
        Class<?> returnType = method.getReturnType();

        //1.访问redis，找到就返回
        String key= prefix+ Arrays.asList(args);
        RBucket<Object> bucket = redissonClient.getBucket(key);
        Object obj = bucket.get();
        if(obj!=null){
            return obj;
        }
        //2.找不到访问数据库
        String lockKey=prefix+RedisConst.SKULOCK_SUFFIX;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            //加锁：缓存击穿
            lock.lock();

            //double check（防止多个同时访问时，一个获得锁后查出来，后面的不需要访问数据库）
            obj = bucket.get();
            if(obj!=null){
                return obj;
            }

            obj = proceedingJoinPoint.proceed(args);

            if(obj==null) {
                //防止缓存穿透：new一个对象
                if (Map.class.equals(returnType)) {
                    obj=new HashMap<>();
                } else if (List.class.equals(returnType)) {
                    obj=new ArrayList<>();
                } else {
                    Constructor<?> declaredConstructor = returnType.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    obj=declaredConstructor.newInstance();
                }
            }
            //防止缓存雪崩
            int i = new Random().nextInt(60);
            bucket.set(obj,120+i, TimeUnit.SECONDS);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        //3.返回结果
        return obj;
    }
}
