package com.example.sort_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SortApplication {

	public static void main(String[] args) {
		SpringApplication.run(SortApplication.class, args);
	}

}
