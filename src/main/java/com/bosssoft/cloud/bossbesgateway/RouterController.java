package com.bosssoft.cloud.bossbesgateway;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boss.bes.core.data.pojo.CommonRequest;
import com.boss.bes.core.data.pojo.CommonResponse;
import com.boss.bes.core.data.pojo.ResultEnum;
import com.boss.bes.core.data.util.ResponseUtil;
import com.bosssoft.cloud.bossbesgateway.util.JwtUtil;
import com.netflix.client.http.HttpResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 网关服务
 * @author likang
 * @date 2019/8/19 9:58
 */
@RestController
public class RouterController {

    /**
     * 熔断回调
     * @return Mono
     */
    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        CommonResponse commonResponse = ResponseUtil.buildError(ResultEnum.GATEWAY_HYSTRIX_TIMEOUT_ERROR);
        return Mono.just(JSONObject.toJSONString(commonResponse));
    }
}
