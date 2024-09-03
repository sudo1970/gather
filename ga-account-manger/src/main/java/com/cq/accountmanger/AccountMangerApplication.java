package com.cq.accountmanger;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.cq.accountmanger.mapper")
@SpringBootApplication
public class AccountMangerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountMangerApplication.class);
    }
}

