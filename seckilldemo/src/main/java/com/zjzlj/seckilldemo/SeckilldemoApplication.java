package com.zjzlj.seckilldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication
public class SeckilldemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckilldemoApplication.class, args);
    }


    /**
     * update 8888888888 for rollback
     */
}
