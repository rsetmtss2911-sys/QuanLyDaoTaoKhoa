package com.qldtkhoa.qldtkhoa_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QldtkhoaAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(QldtkhoaAppApplication.class, args);
	}

}
