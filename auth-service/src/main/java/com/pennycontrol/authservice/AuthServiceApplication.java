package com.pennycontrol.authservice;

import com.pennycontrol.common.annotation.EnableCors;
import com.pennycontrol.common.annotation.EnableExceptionHandling;
import com.pennycontrol.common.annotation.EnableJwt;
import com.pennycontrol.common.annotation.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.pennycontrol.authservice.entity",
        "com.pennycontrol.common.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.pennycontrol.authservice.repository"
})
@EnableJwt
@EnableSecurity
@EnableCors
@EnableExceptionHandling
@EnableScheduling
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
