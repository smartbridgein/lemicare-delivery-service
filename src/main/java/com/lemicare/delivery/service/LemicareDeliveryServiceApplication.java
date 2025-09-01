package com.lemicare.delivery.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LemicareDeliveryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LemicareDeliveryServiceApplication.class, args);
	}

}
