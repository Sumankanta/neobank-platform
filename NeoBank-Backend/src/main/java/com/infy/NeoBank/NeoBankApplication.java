package com.infy.NeoBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class NeoBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeoBankApplication.class, args);
	}

}
