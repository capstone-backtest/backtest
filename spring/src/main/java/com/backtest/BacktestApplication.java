package com.backtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BacktestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BacktestApplication.class, args);
    }
}
