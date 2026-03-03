package com.stevechao.tx_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TxServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TxServiceApplication.class, args);
	}

}
