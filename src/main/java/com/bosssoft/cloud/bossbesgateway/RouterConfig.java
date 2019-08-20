package com.bosssoft.cloud.bossbesgateway;

import com.bosssoft.cloud.bossbesgateway.gateway.OauthFilter;
import com.bosssoft.cloud.bossbesgateway.limiterconfig.MyRedisRateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 路由配置
 * @author likang
 * @date 2019/8/19 8:53
 */
@Configuration
public class RouterConfig {

    @Resource
    private KeyResolver ipKeyResolver;
    @Resource
    private MyRedisRateLimiter myRedisRateLimiter;
    @Resource
    private OauthFilter oauthFilter;

    private static final String PERMISSION_ROUTE_ID = "permission";
    private static final String PERMISSION_ROUTE_PATH = "/boss/bes/permission/**";
    private static final String BASE_DATA_ROUTE_ID = "basedata";
    private static final String BASE_DATA_ROUTE_PATH = "/boss/bes/basedata/**";
    private static final String SYSTEM_ROUTE_ID = "system";
    private static final String SYSTEM_ROUTE_PATH = "/boss/bes/system/**";
    private static final String PAPER_ROUTE_ID = "paper";
    private static final String PAPER_ROUTE_PATH = "/boss/bes/paper/**";
    private static final String EXAM_ROUTE_ID = "exam";
    private static final String EXAM_ROUTE_PATH = "/boss/bes/exam/**";

    /**
     * 权限微服务
     *
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorPermission(RouteLocatorBuilder builder) {
        return builder.routes()
            .route(PERMISSION_ROUTE_ID, r -> r.path(PERMISSION_ROUTE_PATH).and()
                .readBody(Object.class, requestBody -> true)
                .filters(f -> f
                        .stripPrefix(3)
                        .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                        .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                        .filter(oauthFilter)
                )
                .uri("lb://BOSS-BES-PERMISSION")).build();
    }

    /**
     * 基础数据微服务
     *
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorBaseData(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(BASE_DATA_ROUTE_ID, r -> r.path(BASE_DATA_ROUTE_PATH)
                        .filters(f -> f
                                .stripPrefix(3)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                        )
                        .uri("lb://BOSS-BES-BASEDATA")).build();
    }

    /**
     * 系统管理微服务
     *
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorSystem(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(SYSTEM_ROUTE_ID, r -> r.path(SYSTEM_ROUTE_PATH)
                        .filters(f -> f
                                .stripPrefix(3)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                        )
                        .uri("lb://BOSS-BES-SYSTEM")).build();
    }

    /**
     * 试卷管理微服务
     *
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorPaper(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(PAPER_ROUTE_ID, r -> r.path(PAPER_ROUTE_PATH)
                        .filters(f -> f
                                .stripPrefix(3)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                        )
                        .uri("lb://BOSS-BES-PAPER")).build();
    }

    /**
     * 考试管理微服务
     *
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorExam(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(EXAM_ROUTE_ID, r -> r.path(EXAM_ROUTE_PATH)
                        .filters(f -> f
                                .stripPrefix(3)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                        )
                        .uri("lb://BOSS-BES-EXAM")).build();
    }

}
