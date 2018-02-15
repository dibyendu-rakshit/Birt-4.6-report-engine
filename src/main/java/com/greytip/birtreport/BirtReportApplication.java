package com.greytip.birtreport;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@EnableFeignClients
public class BirtReportApplication {

    public static void main(String[] args) {
        //disabled banner, don't want to see the spring logo
        SpringApplication app = new SpringApplication(BirtReportApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}