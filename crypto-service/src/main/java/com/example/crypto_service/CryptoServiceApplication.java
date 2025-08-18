package com.example.crypto_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class CryptoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoServiceApplication.class, args);
	}

}
