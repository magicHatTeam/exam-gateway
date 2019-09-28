package com.bosssoft.cloud.gateway.gateway;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boss.bes.common.utils.JwtUtil;
import com.boss.bes.common.utils.constants.CommonCacheConstants;
import com.boss.bes.core.data.pojo.BaseData;
import com.boss.bes.core.data.pojo.ResultEnum;
import com.boss.bes.core.data.pojo.basevo.BaseModVO;
import com.boss.bes.core.data.pojo.basevo.BaseRemoveVO;
import com.bosssoft.cloud.gateway.Menu;
import com.bosssoft.cloud.gateway.exception.AppException;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * TODO：校验每次请求的删除权限，比对公司ID，超级管理员都能删除...
 * @author likang
 * @date 2019/8/19 22:11
 */
@Component
public class CheckTokenFilter implements GatewayFilter, Ordered {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static String[] whiteList = {"login"};

    private static final Boolean NEED_AUTH_URL = false;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(CommonCacheConstants.REQUEST_TOKEN);
        // 验证token是否存在
        if (StringUtils.isNotEmpty(token)) {
            Map<String, String> tokenMap = JwtUtil.verifyToken(token);
            String userId = tokenMap.get(CommonCacheConstants.USER_ID);
            String data = stringRedisTemplate.opsForValue().get(userId);
            if (data == null) {
                // 无效，提示token失效，需重新登录
                throw new AppException(ResultEnum.TOKEN_INVALID);
            }
            if (!NEED_AUTH_URL){
                // 不需要验证路由权限验证
                return chain.filter(exchange);
            }
            String currentUrl = exchange.getRequest().getHeaders().getFirst("currentUrl");
            // 白名单不需要验证路由
            if (StrUtil.isNotEmpty(currentUrl)){
                for (String url : whiteList) {
                    if (url.equals(currentUrl)){
                        return chain.filter(exchange);
                    }
                }
                // 验证是否具有菜单路由
                JSONObject jsonObject = JSON.parseObject(data);
                JSONArray jsonArray = (JSONArray) jsonObject.get(CommonCacheConstants.ROLES);
                if (jsonArray != null) {
                    List<Long> roles = JSONArray.parseArray(jsonArray.toJSONString(), Long.class);
                    if (roles !=null && roles.size()>0) {
                        List<String> list = new ArrayList<>();
                        roles.forEach(item -> {
                            String urls = stringRedisTemplate.opsForValue().get("menu" + item);
                            List<Menu> urlList = JSONArray.parseArray(urls, Menu.class);
                            if (urlList != null && urlList.size() > 0) {
                                urlList.forEach(url -> list.add(url.getCode()));
                            }
                        });
                        if (list.contains(currentUrl)) {
                            Object requestBody = exchange.getAttribute("cachedRequestBodyObject");
                            Long companyId = jsonObject.getLong(CommonCacheConstants.COMPANY_ID);
                            // 比较修改的数据
                            if (requestBody instanceof BaseModVO){
                                BaseModVO modifyVO = (BaseModVO) requestBody;
                                if (modifyVO.getLoginCompanyId().equals(companyId)){
                                    return chain.filter(exchange);
                                }
                            }
                            // 比较删除的数据
                            else if (requestBody instanceof BaseRemoveVO) {
                                BaseRemoveVO removeVO = (BaseRemoveVO) requestBody;
                                List<BaseData> dataList = removeVO.getDataList();
                                for (BaseData item : dataList) {
                                    if (!item.getCompanyId().equals(companyId)){
                                        throw new AppException(ResultEnum.REQUEST_NO_AUTH_ERROR);
                                    }
                                }
                            } else {
                                return chain.filter(exchange);
                            }
                        }
                    }
                }
            }
            throw new AppException(ResultEnum.REQUEST_NO_AUTH_ERROR);
        }else {
            // 不存在，提示未登录，重新登录
            throw new AppException(ResultEnum.TOKEN_NOT_EXITS);
        }
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
