package com.financialapp.banks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "com.financialapp.banks.infrastructure.client")
public class BanksApplication {

    public static void main(String[] args) {
        SpringApplication.run(BanksApplication.class, args);
    }
}
