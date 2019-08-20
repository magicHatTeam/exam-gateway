package com.bosssoft.cloud.bossbesgateway.limiterconfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Min;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于redis的限流实现类
 * copy于RedisRateLimiter源码
 * 增加了一个设置令牌数据的方法
 * @author likang
 * @date 2019/8/19 13:58
 */
public class MyRedisRateLimiter extends AbstractRateLimiter<RedisRateLimiter.Config>
        implements ApplicationContextAware {

    @Deprecated
    public static final String REPLENISH_RATE_KEY = "replenishRate";

    @Deprecated
    public static final String BURST_CAPACITY_KEY = "burstCapacity";

    public static final String CONFIGURATION_PROPERTY_NAME = "redis-rate-limiter";
    public static final String REDIS_SCRIPT_NAME = "redisRequestRateLimiterScript";
    public static final String REMAINING_HEADER = "X-RateLimit-Remaining";
    public static final String REPLENISH_RATE_HEADER = "X-RateLimit-Replenish-Rate";
    public static final String BURST_CAPACITY_HEADER = "X-RateLimit-Burst-Capacity";

    private Log log = LogFactory.getLog(getClass());

    private ReactiveRedisTemplate<String, String> redisTemplate;

    private RedisScript<List<Long>> script;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private RedisRateLimiter.Config defaultConfig;

    private boolean includeHeaders = true;
    private String remainingHeader = REMAINING_HEADER;
    private String replenishRateHeader = REPLENISH_RATE_HEADER;
    private String burstCapacityHeader = BURST_CAPACITY_HEADER;

    public MyRedisRateLimiter(ReactiveRedisTemplate<String, String> redisTemplate,
                            RedisScript<List<Long>> script, Validator validator) {
        super(RedisRateLimiter.Config.class, CONFIGURATION_PROPERTY_NAME, validator);
        this.redisTemplate = redisTemplate;
        this.script = script;
        initialized.compareAndSet(false, true);
    }

    public MyRedisRateLimiter(int defaultReplenishRate, int defaultBurstCapacity) {
        super(RedisRateLimiter.Config.class, CONFIGURATION_PROPERTY_NAME, null);
        this.defaultConfig = new RedisRateLimiter.Config().setReplenishRate(defaultReplenishRate)
                .setBurstCapacity(defaultBurstCapacity);
    }

    static List<String> getKeys(String id) {
        String prefix = "request_rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    public void setLimiterConfig(int defaultReplenishRate, int defaultBurstCapacity){
        this.defaultConfig = new RedisRateLimiter.Config().setReplenishRate(defaultReplenishRate)
                .setBurstCapacity(defaultBurstCapacity);
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public String getRemainingHeader() {
        return remainingHeader;
    }

    public void setRemainingHeader(String remainingHeader) {
        this.remainingHeader = remainingHeader;
    }

    public String getReplenishRateHeader() {
        return replenishRateHeader;
    }

    public void setReplenishRateHeader(String replenishRateHeader) {
        this.replenishRateHeader = replenishRateHeader;
    }

    public String getBurstCapacityHeader() {
        return burstCapacityHeader;
    }

    public void setBurstCapacityHeader(String burstCapacityHeader) {
        this.burstCapacityHeader = burstCapacityHeader;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (initialized.compareAndSet(false, true)) {
            this.redisTemplate = context.getBean("stringReactiveRedisTemplate",
                    ReactiveRedisTemplate.class);
            this.script = context.getBean(REDIS_SCRIPT_NAME, RedisScript.class);
            if (context.getBeanNamesForType(Validator.class).length > 0) {
                this.setValidator(context.getBean(Validator.class));
            }
        }
    }

    /* for testing */ RedisRateLimiter.Config getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Response> isAllowed(String routeId, String id) {
        if (!this.initialized.get()) {
            throw new IllegalStateException("RedisRateLimiter is not initialized");
        }

        RedisRateLimiter.Config routeConfig = loadConfiguration(routeId);
        int replenishRate = routeConfig.getReplenishRate();
        int burstCapacity = routeConfig.getBurstCapacity();

        try {
            List<String> keys = getKeys(id);
            List<String> scriptArgs = Arrays.asList(replenishRate + "",
                    burstCapacity + "", Instant.now().getEpochSecond() + "", "1");
            Flux<List<Long>> flux = this.redisTemplate.execute(this.script, keys,
                    scriptArgs);
            return flux.onErrorResume(throwable -> Flux.just(Arrays.asList(1L, -1L)))
                    .reduce(new ArrayList<Long>(), (longs, l) -> {
                        longs.addAll(l);
                        return longs;
                    }).map(results -> {
                        boolean allowed = results.get(0) == 1L;
                        Long tokensLeft = results.get(1);

                        Response response = new Response(allowed,
                                getHeaders(routeConfig, tokensLeft));

                        if (log.isDebugEnabled()) {
                            log.debug("response: " + response);
                        }
                        return response;
                    });
        }
        catch (Exception e) {
            log.error("Error determining if user allowed from redis", e);
        }
        return Mono.just(new Response(true, getHeaders(routeConfig, -1L)));
    }

    /* for testing */ RedisRateLimiter.Config loadConfiguration(String routeId) {
        RedisRateLimiter.Config routeConfig = getConfig().getOrDefault(routeId, defaultConfig);

        if (routeConfig == null) {
            routeConfig = getConfig().get(RouteDefinitionRouteLocator.DEFAULT_FILTERS);
        }

        if (routeConfig == null) {
            throw new IllegalArgumentException(
                    "No Configuration found for route " + routeId + " or defaultFilters");
        }
        return routeConfig;
    }

    public Map<String, String> getHeaders(RedisRateLimiter.Config config, Long tokensLeft) {
        Map<String, String> headers = new HashMap<>();
        if (isIncludeHeaders()) {
            headers.put(this.remainingHeader, tokensLeft.toString());
            headers.put(this.replenishRateHeader,
                    String.valueOf(config.getReplenishRate()));
            headers.put(this.burstCapacityHeader,
                    String.valueOf(config.getBurstCapacity()));
        }
        return headers;
    }

    @Validated
    public static class Config {

        @Min(1)
        private int replenishRate;

        @Min(1)
        private int burstCapacity = 1;

        public int getReplenishRate() {
            return replenishRate;
        }

        public MyRedisRateLimiter.Config setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
            return this;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public MyRedisRateLimiter.Config setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
            return this;
        }

        @Override
        public String toString() {
            return "Config{" + "replenishRate=" + replenishRate + ", burstCapacity="
                    + burstCapacity + '}';
        }

    }
}
