package com.bosssoft.cloud.bossbesgateway;

import com.alibaba.fastjson.JSONObject;
import com.boss.bes.core.data.util.ResponseUtil;
import com.boss.bes.core.data.vo.CommonRequest;
import com.boss.bes.core.data.vo.CommonResponse;
import com.boss.bes.core.data.vo.ResultEnum;
import com.bosssoft.cloud.bossbesgateway.util.JwtUtil;
import com.netflix.client.http.HttpResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 网关服务
 * @author likang
 * @date 2019/8/19 9:58
 */
@RestController
public class RouterController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 熔断回调
     * @return Mono
     */
    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        CommonResponse commonResponse = ResponseUtil.buildError(ResultEnum.GATEWAY_HYSTRIX_TIMEOUT_ERROR);
        return Mono.just(JSONObject.toJSONString(commonResponse));
    }

    @PostMapping("/login")
    public Mono<CommonResponse> loginMock(@RequestBody CommonRequest<Map<String,Object>> request){
        Map<String, Object> body = request.getBody();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("createdBy", body.get("username"));
        jsonObject.put("updatedBy", body.get("username"));
        jsonObject.put("companyId", body.get("companyId"));
        jsonObject.put("companyName", body.get("companyName"));
        jsonObject.put("orgId", body.get("orgId"));
        jsonObject.put("orgName", body.get("orgName"));
        stringRedisTemplate.opsForValue().set(body.get("username").toString(),jsonObject.toJSONString(),12+(new Random().nextInt(10)), TimeUnit.HOURS);
        Map<String,String> map = new HashMap<>(1);
        map.put("id",body.get("username").toString());
        jsonObject.clear();
        jsonObject.put("token", JwtUtil.genToken(map));
        jsonObject.put("version", "1");
        jsonObject.put("businessType", "123");
        jsonObject.put("deviceId", "1");
        jsonObject.put("deviceType", 1);
        jsonObject.put("crypt", 1);
        CommonResponse commonResponse = ResponseUtil.buildSuccess(jsonObject);
        return Mono.just(commonResponse);
    }

    @PostMapping("/info")
    public Mono<CommonResponse> infoMock(@RequestBody CommonRequest<Map<String,Object>> request){
        CommonResponse commonResponse = ResponseUtil.buildSuccess();
        return Mono.just(commonResponse);
    }

    @PostMapping("/logout")
    public Mono<CommonResponse> logoutMock(@RequestBody CommonRequest<Map<String,Object>> request){
        Map<String,String> map = new HashMap<>(1);
        map.put("data","success");
        CommonResponse commonResponse = ResponseUtil.buildSuccess(map);
        return Mono.just(commonResponse);
    }
}
