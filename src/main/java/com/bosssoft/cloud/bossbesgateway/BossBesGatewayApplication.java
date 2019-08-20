package com.bosssoft.cloud.bossbesgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 网关启动类
 * @author likang
 */
@SpringBootApplication
@EnableEurekaClient
public class BossBesGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BossBesGatewayApplication.class, args);
    }

}
