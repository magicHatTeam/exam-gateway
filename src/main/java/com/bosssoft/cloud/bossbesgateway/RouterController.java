package com.bosssoft.cloud.bossbesgateway;

import com.alibaba.fastjson.JSONObject;
import com.boss.bes.core.data.util.ResponseUtil;
import com.boss.bes.core.data.vo.CommonResponse;
import com.boss.bes.core.data.vo.ResultEnum;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
