package com.oauthlogin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.oauthlogin.config.properties.AppProperties;
import com.oauthlogin.config.properties.CorsProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        CorsProperties.class,
        AppProperties.class
})
public class OauthLoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(OauthLoginApplication.class, args);
    }

}
