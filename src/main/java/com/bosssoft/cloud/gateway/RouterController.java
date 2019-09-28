package com.bosssoft.cloud.gateway;

import com.boss.bes.core.data.pojo.CommonResponse;
import com.boss.bes.core.data.pojo.ResultEnum;
import com.boss.bes.core.data.util.ResponseUtil;
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
    public Mono<CommonResponse> fallback() {
        return Mono.just(ResponseUtil.buildError(ResultEnum.GATEWAY_HYSTRIX_TIMEOUT_ERROR));
    }
}
