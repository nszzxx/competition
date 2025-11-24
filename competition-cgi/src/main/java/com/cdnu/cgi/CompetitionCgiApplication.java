package com.cdnu.cgi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class CompetitionCgiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompetitionCgiApplication.class, args);
    }

}
