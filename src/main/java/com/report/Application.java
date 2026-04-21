package com.report;

import com.report.module.preload.active.EnableModuleLoad;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableModuleLoad
@SpringBootApplication(scanBasePackages = {"com.report.module.preload","com.report.common"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
