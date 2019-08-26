package com.bosssoft.cloud.bossbesgateway.gateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boss.bes.core.data.pojo.ResultEnum;
import com.bosssoft.cloud.bossbesgateway.exception.AppException;
import com.bosssoft.cloud.bossbesgateway.util.JwtUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.*;

/**
 * 权限验证拦截器 - 业务模块需要添加此拦截器
 * 验证是否存在Token
 * 并验证Token的有效性
 ***********
 * 验证路由是否有权限
 * 暂未实现，实现思路，由redis实现
 * 先从token里面拿用户ID
 * 再从redis里面拿用户角色
 * 根据访问路由与redis里面缓存的路由比较，角色一致放行，不一致报没权限异常
 * @author likang
 * @date 2019/8/19 22:11
 */
@Component
public class CheckTokenFilter implements GatewayFilter, Ordered {
    private static Logger logger = LoggerFactory.getLogger(CheckTokenFilter.class);
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String REQUEST_TOKEN_STRING = "token";
    private static final String REQUEST_TOKEN_ID_STRING = "id";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(REQUEST_TOKEN_STRING);
        // 验证token是否存在
        if (StringUtils.isNotEmpty(token)) {
            Map<String, String> tokenMap = JwtUtil.verifyToken(token);
            String userId = tokenMap.get(REQUEST_TOKEN_ID_STRING);
            String data = stringRedisTemplate.opsForValue().get(userId);
            if (data != null) {
                String uri = String.valueOf(exchange.getRequest().getPath());
                if (uri!=null) {
                    JSONObject jsonObject = JSON.parseObject(data);
                    JSONArray jsonArray = (JSONArray) jsonObject.get("roles");
                    List<String> roles = JSONArray.parseArray(jsonArray.toJSONString(), String.class);
                    List<String> list = new ArrayList<>();
                    roles.forEach(item -> {
                        String urls = stringRedisTemplate.opsForValue().get(item);
                        List<String> urlList = JSONArray.parseArray(urls, String.class);
                        if (urlList != null && urlList.size() > 0) {
                            list.addAll(urlList);
                        }
                    });
                    if (list.contains(uri)) {
                        return chain.filter(exchange);
                    }else{
                        throw new AppException(ResultEnum.REQUEST_NO_AUTH_ERROR);
                    }
                }else {
                    throw new AppException(ResultEnum.SYSTEM_ERROR);
                }
            } else {
                // 无效，提示token失效，需重新登录
                throw new AppException(ResultEnum.TOKEN_INVALID);
            }
        } else {
            // 不存在，提示未登录，重新登录
            throw new AppException(ResultEnum.TOKEN_NOT_EXITS);
        }
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
