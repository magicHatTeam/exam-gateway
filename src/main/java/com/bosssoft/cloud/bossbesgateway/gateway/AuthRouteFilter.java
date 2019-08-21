package com.bosssoft.cloud.bossbesgateway.gateway;

import com.bosssoft.cloud.bossbesgateway.RouterConfig;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 验证路由是否有权限
 * 暂未实现，实现思路，由redis实现
 * 先从token里面拿用户ID
 * 再从redis里面拿用户角色
 * 根据访问路由与redis里面缓存的路由比较，角色一致放行，不一致报没权限异常
 * @author likang
 * @date 2019/8/20 19:46
 */
@Component
public class AuthRouteFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
