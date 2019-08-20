package com.bosssoft.cloud.bossbesgateway.gateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boss.bes.core.data.util.ResponseUtil;
import com.boss.bes.core.data.vo.CommonResponse;
import com.boss.bes.core.data.vo.ResultEnum;
import com.bosssoft.cloud.bossbesgateway.exception.AppException;
import com.bosssoft.cloud.bossbesgateway.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 验证是否存在Token
 * 并验证Token的有效性
 * @author likang
 * @date 2019/8/19 22:11
 */
@Component
public class OauthFilter implements GatewayFilter, Ordered {
    private static Logger logger = LoggerFactory.getLogger(OauthFilter.class);
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String REQUEST_CACHE_STRING = "cachedRequestBodyObject";
    private static final String REQUEST_HEAD_STRING = "head";
    private static final String REQUEST_TOKEN_STRING = "token";
    private static final String REQUEST_TOKEN_ID_STRING = "id";
    private static final Integer REQUEST_BODY_LENGTH = 2;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Map<String,Map<String,Object>> requestBody = exchange.getAttribute(REQUEST_CACHE_STRING);
        if (requestBody != null && requestBody.size() == REQUEST_BODY_LENGTH) {
            Map<String,Object> head = requestBody.get(REQUEST_HEAD_STRING);
            if (head != null && head.size() > 0) {
                if (head.containsKey(REQUEST_TOKEN_STRING)){
                    String token = head.get(REQUEST_TOKEN_STRING).toString();
                    // 验证token是否存在
                    if (!token.isEmpty()) {
                        Map<String, String> tokenMap = JwtUtil.verifyToken(token);
                        String userId = tokenMap.get(REQUEST_TOKEN_ID_STRING);
                        String data = stringRedisTemplate.opsForValue().get(userId);
                        if (data != null) {
                            // 在redis验证token是否有效
                            return chain.filter(exchange);
                        } else {
                            // 无效，提示token失效，需重新登录
                            throw new AppException(ResultEnum.TOKEN_INVALID);
                        }
                    } else {
                        // 不存在，提示未登录，重新登录
                        throw new AppException(ResultEnum.TOKEN_NOT_EXITS);
                    }
                }
            }
        }
        throw new AppException(ResultEnum.REQUEST_BODY_PARAMS_ERROR);
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
