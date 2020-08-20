package com.backend;

import com.backend.config.PartnerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class Application {
	private static final String PATH_TO_CONFIG_FOLDER = "conf\\";

	@PostConstruct
	public void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+7:00"));
	}

	public static void main(String[] args) {
		PartnerConfig.init(PATH_TO_CONFIG_FOLDER + "partner.json");
		SpringApplication.run(Application.class, args);
	}
}