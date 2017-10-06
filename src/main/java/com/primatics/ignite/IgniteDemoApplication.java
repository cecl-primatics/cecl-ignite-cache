package com.primatics.ignite;

import org.apache.ignite.IgniteException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IgniteDemoApplication {

	public static void main(String[] args) throws IgniteException {
		SpringApplication.run(IgniteDemoApplication.class, args);
	}
}
