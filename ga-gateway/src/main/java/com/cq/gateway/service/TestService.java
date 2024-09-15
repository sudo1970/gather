package com.cq.gateway.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @SentinelResource(value="sayHello")
    public String sayHello(String name) {
        return "hello," + name;
    }
}
