package com.bosssoft.cloud.bossbesgateway.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author likang
 * @date 2019/8/19 22:11
 */
@Component
public class OauthFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Object requestBody = exchange.getAttribute("cachedRequestBodyObject");
        System.out.println("OauthFilter");
        System.out.println(requestBody);
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            System.out.println("RequestFilter post filter");
        }));
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
