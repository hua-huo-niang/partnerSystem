package com.qiang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.qiang.mapper")
public class PartnerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerBackendApplication.class, args);
    }

}
