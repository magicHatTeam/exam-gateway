package com.bosssoft.cloud.gateway.limiterconfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 限流配置类
 * @author likang
 * @date 2019/8/19 11:23
 */
@Configuration
public class LimiterConfig {

    /**
     * 令牌桶总容量
     */
    @Value("${limiter.replenishRate}")
    private Integer replenishRate;
    /**
     * 令牌桶每秒填充平均速率
     */
    @Value("${limiter.burstCapacity}")
    private Integer burstCapacity;

    /**
     * IP限流
     * @return KeyResolver
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostString());
    }

    /**
     * 用户限流
     * @return KeyResolver
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getQueryParams().getFirst("userId"));
    }

    /**
     * 接口限流
     * @return KeyResolver
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

    /**
     * redis 限流配置类
     * @param redisTemplate redisTemplate
     * @param redisScript redisScript
     * @param defaultValidator defaultValidator
     * @return MyRedisRateLimiter
     */
    @Bean
    @Primary
    public MyRedisRateLimiter myRedisRateLimiter(
            ReactiveRedisTemplate<String, String> redisTemplate,
            @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
            Validator defaultValidator) {
        MyRedisRateLimiter myRedisRateLimiter = new MyRedisRateLimiter(redisTemplate,redisScript,defaultValidator);
        myRedisRateLimiter.setLimiterConfig(replenishRate,burstCapacity);
        return myRedisRateLimiter;
    }
}
