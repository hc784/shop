package com.shop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siot.IamportRestClient.IamportClient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "imp.api")
public class IamportConfig {
    private String key;
    private String secretkey;

    @Bean
    public IamportClient iamportClient() {
        return new IamportClient(key, secretkey);
    }
}
