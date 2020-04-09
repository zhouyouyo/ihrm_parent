package com.ihrm.gate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication(scanBasePackages = "com.ihrm")//主要扫描common下的信息
@EnableZuulProxy //申明zuul网关工程,开启zuul相关注解
@EnableDiscoveryClient //开启服务发现功能
public class GateApplication {
    public static void main(String[] args) {
        SpringApplication.run(GateApplication.class);
    }
}
