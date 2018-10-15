package com.sunsan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(
        basePackages = {"com.sunsan"}
)
public class SpringbootBeatlsqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootBeatlsqlApplication.class, args);
	}

}
