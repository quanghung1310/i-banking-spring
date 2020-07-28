package com.backend;

import com.backend.config.PartnerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	private static final String PATH_TO_CONFIG_FOLDER = "conf\\";

	public static void main(String[] args) {
		PartnerConfig.init(PATH_TO_CONFIG_FOLDER + "partner.json");
		SpringApplication.run(Application.class, args);
	}
}