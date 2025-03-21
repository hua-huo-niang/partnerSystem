package com.qiang.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类，创建Redisson客户端，来帮我们操作redisson
 * 这样我们使用redis就可以很方便，像使用java中的集合
 */
@Configuration
@ConfigurationProperties("spring.data.redis")
@Data
public class RedissionConfig {
    private String host;
    private String port;
    private String password;
    private Integer redissonDataBase;
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress(String.format("redis://%s:%s",host,port)).
                setPassword(password).setDatabase(redissonDataBase);
        return Redisson.create(config);
    }
}
