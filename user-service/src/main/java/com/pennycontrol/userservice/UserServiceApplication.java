package com.pennycontrol.userservice;

import com.pennycontrol.common.annotation.EnableCors;
import com.pennycontrol.common.annotation.EnableExceptionHandling;
import com.pennycontrol.common.annotation.EnableJwt;
import com.pennycontrol.common.annotation.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJwt
@EnableSecurity
@EnableCors
@EnableExceptionHandling
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
