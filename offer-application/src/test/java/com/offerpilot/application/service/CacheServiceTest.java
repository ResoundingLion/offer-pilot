package com.offerpilot.application.service;

import com.offerpilot.application.service.CacheService.NullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * CacheService 单元测试
 *
 * 覆盖：getOrLoad 缓存命中/未命中/空值缓存防穿透、evict
 * 注意：由于 CacheService 使用字段注入（@Autowired），通过 ReflectionTestUtils 手动注入 mock
 */
@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        // CacheService 使用 @Autowired 字段注入，通过 ReflectionTestUtils 手动注入 mock
        cacheService = new CacheService();
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        ReflectionTestUtils.setField(cacheService, "redisTemplate", redisTemplate);
    }

    // ========================================================================
    // 1. getOrLoad — 缓存命中
    // ========================================================================

    @Nested
    @DisplayName("缓存命中")
    class CacheHit {

        @Test
        @DisplayName("缓存命中时直接返回缓存值，不调用 loader")
        void hitReturnsCachedValue() {
            String key = "company:1";
            String cached = "字节跳动";
            when(valueOps.get(key)).thenReturn(cached);

            AtomicInteger loadCount = new AtomicInteger(0);
            Supplier<String> loader = () -> {
                loadCount.incrementAndGet();
                return "字节跳动_new";
            };

            String result = cacheService.getOrLoad(key, String.class, loader);

            assertThat(result).isEqualTo("字节跳动");
            assertThat(loadCount.get()).isZero(); // loader 未被调用
            verify(valueOps, never()).set(any(), any(), any(Duration.class));
        }

        @Test
        @DisplayName("缓存命中的对象类型与期望一致")
        void hitWithCorrectType() {
            when(valueOps.get("num")).thenReturn(42);

            Integer result = cacheService.getOrLoad("num", Integer.class, () -> 0);
            assertThat(result).isEqualTo(42);
        }
    }

    // ========================================================================
    // 2. getOrLoad — 缓存未命中
    // ========================================================================

    @Nested
    @DisplayName("缓存未命中")
    class CacheMiss {

        @Test
        @DisplayName("缓存未命中时调用 loader 并写入缓存")
        void missLoadsAndCaches() {
            String key = "company:2";
            when(valueOps.get(key)).thenReturn(null);

            String result = cacheService.getOrLoad(key, String.class, () -> "阿里巴巴");

            assertThat(result).isEqualTo("阿里巴巴");
            verify(valueOps).set(eq(key), eq("阿里巴巴"), any(Duration.class));
        }

        @Test
        @DisplayName("loader 只调用一次（幂等验证）")
        void loaderCalledOnlyOnce() {
            String key = "company:3";
            when(valueOps.get(key)).thenReturn(null);

            AtomicInteger callCount = new AtomicInteger(0);
            cacheService.getOrLoad(key, String.class, () -> {
                callCount.incrementAndGet();
                return "腾讯";
            });

            assertThat(callCount.get()).isEqualTo(1);
        }
    }

    // ========================================================================
    // 3. getOrLoad — 空值缓存防穿透
    // ========================================================================

    @Nested
    @DisplayName("空值缓存防穿透")
    class NullValueCaching {

        @Test
        @DisplayName("loader 返回 null 时写入空值缓存")
        void nullResultCachesNullValue() {
            String key = "company:999";
            when(valueOps.get(key)).thenReturn(null);

            String result = cacheService.getOrLoad(key, String.class, () -> null);

            assertThat(result).isNull();
            // 验证写入了 NullValue 标记，TTL 60s
            verify(valueOps).set(eq(key), eq(NullValue.INSTANCE), eq(Duration.ofSeconds(60)));
        }

        @Test
        @DisplayName("空值缓存命中时返回 null 且不调用 loader")
        void nullValueHitReturnsNull() {
            String key = "company:999";
            when(valueOps.get(key)).thenReturn(NullValue.INSTANCE);

            AtomicInteger loadCount = new AtomicInteger(0);
            String result = cacheService.getOrLoad(key, String.class, () -> {
                loadCount.incrementAndGet();
                return "不应被调用";
            });

            assertThat(result).isNull();
            assertThat(loadCount.get()).isZero(); // loader 未被调用
            verify(valueOps, never()).set(any(), any(), any(Duration.class));
        }
    }

    // ========================================================================
    // 4. evict
    // ========================================================================

    @Nested
    @DisplayName("evict 删除缓存")
    class Evict {

        @Test
        @DisplayName("evict 调用 redisTemplate.delete")
        void evictDeletesKey() {
            cacheService.evict("company:1");
            verify(redisTemplate).delete("company:1");
        }

        @Test
        @DisplayName("evict 不存在的 key 不抛异常")
        void evictNonExistent() {
            cacheService.evict("non_existent_key");
            verify(redisTemplate).delete("non_existent_key");
        }
    }
}
