package com.lin.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lin.**")
public class SpringSecurityPracticesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityPracticesApplication.class, args);
	}

}
