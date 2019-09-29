package com.bosssoft.cloud.gateway;

import com.bosssoft.cloud.gateway.gateway.CheckTokenFilter;
import com.bosssoft.cloud.gateway.limiterconfig.MyRedisRateLimiter;
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
    private CheckTokenFilter checkTokenFilter;

    /**
     * 权限路由ID
     */
    private static final String PERMISSION_ROUTE_ID = "permission";
    /**
     * 权限路由路径
     */
    private static final String PERMISSION_ROUTE_PATH = "/boss/bes/permission/**";
    /**
     * 基础数据路由ID
     */
    private static final String BASE_DATA_ROUTE_ID = "basedata";
    /**
     * 基础数据路由路径
     */
    private static final String BASE_DATA_ROUTE_PATH = "/boss/bes/basedata/**";
    /**
     * 系统服务路由ID
     */
    private static final String SYSTEM_ROUTE_ID = "system";
    /**
     * 系统服务路由路径
     */
    private static final String SYSTEM_ROUTE_PATH = "/boss/bes/system/**";
    /**
     * 试卷服务路由ID
     */
    private static final String PAPER_ROUTE_ID = "paper";
    /**
     * 试卷服务路由路径
     */
    private static final String PAPER_ROUTE_PATH = "/boss/bes/paper/**";
    /**
     * 考试路由ID
     */
    private static final String EXAM_ROUTE_ID = "exam";
    /**
     * 考试路由路径
     */
    private static final String EXAM_ROUTE_PATH = "/boss/bes/exam/**";
    /**
     * 学生考试路由ID
     */
    private static final String TEST_ROUTE_ID = "test";
    /**
     * 学生考试路由路径
     */
    private static final String TEST_ROUTE_PATH = "/boss/bes/test/**";

    /**
     * 图片上传，不校验token
     * 该路径需要在其他业务路径前面，否则会报415类型错误
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorUploadFile(RouteLocatorBuilder builder) {
        return builder.routes()
                // 考试服务上传文件
                .route("file_exam", r -> r.path("/boss/bes/file/exam/**")
                        .filters(f -> f.stripPrefix(4)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                                .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                        )
                        .uri("lb://BOSS-BES-EXAM"))
                // 基础数据服务上传文件
                .route("file_base", r -> r.path("/boss/bes/file/basedata/**")
                        .filters(f -> f.stripPrefix(4)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                                .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                        )
                        .uri("lb://BOSS-BES-BASEDATA"))
                // 系统服务上传文件
                .route("file_system", r -> r.path("/boss/bes/file/system/**")
                        .filters(f -> f.stripPrefix(4)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                                .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                        )
                        .uri("lb://BOSS-BES-SYSTEM"))
                .build();
    }

    /**
     * 权限微服务，不校验token
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorPermission(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(PERMISSION_ROUTE_ID, r -> r.path(PERMISSION_ROUTE_PATH).and().readBody(Object.class, requestBody -> true)
                    .filters(f -> f.stripPrefix(3)
                            .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                            .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                    )
                    .uri("lb://BOSS-BES-PERMISSION")
                )
                .build();
    }

    /**
     * 基础数据微服务
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorBaseData(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(BASE_DATA_ROUTE_ID, r -> r.path(BASE_DATA_ROUTE_PATH).and().readBody(Object.class, requestBody -> true)
                        .filters(f -> f.stripPrefix(3)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                                .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                                .filter(checkTokenFilter)
                        )
                        .uri("lb://BOSS-BES-BASEDATA")).build();
    }

    /**
     * 系统管理微服务
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorSystem(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(SYSTEM_ROUTE_ID, r -> r.path(SYSTEM_ROUTE_PATH).and().readBody(Object.class, requestBody -> true)
                        .filters(f -> f.stripPrefix(3)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                                .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                                .filter(checkTokenFilter)
                        )
                        .uri("lb://BOSS-BES-SYSTEM")).build();
    }

    /**
     * 试卷管理微服务
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorPaper(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(PAPER_ROUTE_ID, r -> r.path(PAPER_ROUTE_PATH).and().readBody(Object.class, requestBody -> true)
                        .filters(f -> f.stripPrefix(3)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                                .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                                .filter(checkTokenFilter)
                        )
                        .uri("lb://BOSS-BES-PAPER")).build();
    }

    /**
     * 考试管理微服务
     * @param builder 路由设置
     * @return RouteLocator
     */
    @Bean
    public RouteLocator routeLocatorExam(RouteLocatorBuilder builder) {
        return builder.routes()
                // 考试管理路由
                .route(EXAM_ROUTE_ID, r -> r.path(EXAM_ROUTE_PATH).and().readBody(Object.class, requestBody -> true)
                        .filters(f -> f.stripPrefix(3)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                                .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                                .filter(checkTokenFilter)
                        )
                        .uri("lb://BOSS-BES-EXAM"))
                // 考试路由
                .route(TEST_ROUTE_ID, r -> r.path(TEST_ROUTE_PATH).and().readBody(Object.class, requestBody -> true)
                        .filters(f -> f.stripPrefix(3)
                                .hystrix(config -> config.setName("hystrixName").setFallbackUri("forward:/fallback"))
                                .requestRateLimiter(config -> config.setKeyResolver(ipKeyResolver).setRateLimiter(myRedisRateLimiter))
                        )
                        .uri("lb://BOSS-BES-EXAM"))
                .build();
    }

}
