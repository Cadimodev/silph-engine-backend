package com.silphengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SilphEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SilphEngineApplication.class, args);
	}

}
