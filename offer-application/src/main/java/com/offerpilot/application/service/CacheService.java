package com.offerpilot.application.service;

import com.offerpilot.application.config.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 空值缓存有效期
    private static final Duration NULL_TTL = Duration.ofSeconds(60);

    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        // 1、尝试从缓存获取
        Object cached = ops.get(key);
        if (cached != null) {
            if(cached instanceof NullValue) {
                return null;
            }
            return (T) cached;
        }

        // 2. 缓存未命中，加载数据
        T result = loader.get();

        // 3. 写入缓存
        if (result != null) {
            ops.set(key, result, CacheConfig.getRandomTtl());
        } else {
            // 空值缓存：下次查同一个 ID 不再穿透
            ops.set(key, NullValue.INSTANCE, NULL_TTL);
        }

        return result;
    }

    /**
     * 删除缓存
     */
    public void evict(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 空值标记（防缓存穿透用）
     * 枚举单例，Jackson 序列化友好
     */
    public enum NullValue {
        INSTANCE
    }

}
