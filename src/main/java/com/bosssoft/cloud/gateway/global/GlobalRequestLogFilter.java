package com.bosssoft.cloud.gateway.global;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关日志记录-request
 * @author likang
 * @date 2019/8/19 22:37
 */
@Component
public class GlobalRequestLogFilter implements GlobalFilter, Ordered {

    private static Logger logger = LoggerFactory.getLogger(GlobalRequestLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        logger.info("path: " + request.getPath());
        logger.info("address: " + request.getRemoteAddress());
        logger.info("method: " + request.getMethodValue());
        logger.info("URI: " + request.getURI());
        logger.info("Headers: " + request.getHeaders());
        Object requestBody = exchange.getAttribute("cachedRequestBodyObject");
        logger.info("body: "+ requestBody);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
