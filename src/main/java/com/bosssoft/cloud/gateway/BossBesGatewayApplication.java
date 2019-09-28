package com.bosssoft.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 网关启动类
 * 已完成功能：
 *      网关的异常处理、日志记录、限流、熔断
 * 未完成的功能：
 *      请求的黑白名单
 * @author likang
 */
@SpringBootApplication
@EnableEurekaClient
public class BossBesGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BossBesGatewayApplication.class, args);
    }

}
