package com.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;


@SpringBootApplication
@RestController
public class Application {
	private static final Logger logger = LogManager.getLogger(Application.class);
	@GetMapping("/")
	String home() {
		return "Hello!";
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		logger.debug("Debugging log");
		logger.info("Info log");
		logger.warn("Hey, This is a warning!");
		logger.error("Oops! We have an Error. OK");
		logger.fatal("Damn! Fatal error. Please fix me.");
	}
}