package com.backend;

import com.backend.config.MainConfig;
import com.backend.config.PartnerConfig;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@SpringBootApplication
@RestController
public class Application {
	private static final Logger LOGGER = LogManager.getLogger(Application.class);
	private static final String PATH_TO_CONFIG_FOLDER = "conf\\";

	public static WebClient webClient;

	public static void main(String[] args) throws IOException {
		JsonObject mainConfig = new JsonObject(new String(Files.readAllBytes(Paths.get(PATH_TO_CONFIG_FOLDER + "main.json"))));
		MainConfig.setMainConfig(mainConfig);
        PartnerConfig.init(PATH_TO_CONFIG_FOLDER + "partner.json");
		JsonObject verticlesConfig = new JsonObject(new String(Files.readAllBytes(Paths.get(PATH_TO_CONFIG_FOLDER + "verticles.json"))));
		Vertx vertx = Vertx.vertx();
		webClient = WebClient.create(vertx, new WebClientOptions().setTrustAll(true));
		deployVerticle(vertx, verticlesConfig);

		DatabindCodec.mapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		DatabindCodec.mapper().configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
	}

	private static void deployVerticle(Vertx vertx, JsonObject verticles) {
		if (!verticles.isEmpty()) {
			verticles.getJsonArray("verticles", new JsonArray())
					.stream()
					.map(o -> (JsonObject)o)
					.forEach(verticle -> {
						boolean isDeployed = verticle.getBoolean("isDeployed", true);
						final String nameOfVerticle = verticle.getString("name", "");
						if (isDeployed) {
							boolean isBlock = verticle.getBoolean("block", false);
							final int numberInstance = verticle.getInteger("instances", 1);
							DeploymentOptions deploymentOptions = new DeploymentOptions();
							if(isBlock) {
								deploymentOptions.setWorker(true);
								String poolName = verticle.getString("poolName", "");
								if (StringUtils.isNoneBlank(poolName)) {
									deploymentOptions.setWorkerPoolName(poolName)
											.setWorkerPoolSize(numberInstance);
								}
							}
							deploymentOptions
									.setInstances(numberInstance)
									.setHa(true);
							LOGGER.info("==================== Start deploy verticle {} ====================", nameOfVerticle);
							final String addressOfVerticle = verticle.getString("address", "");
							vertx.deployVerticle(addressOfVerticle, deploymentOptions, res -> {
								if (res.succeeded()) {
									LOGGER.info("-------------------- {} deploy successfully --------------------", nameOfVerticle);
								} else {
									LOGGER.error("____________________ {} deploy fail ____________________", nameOfVerticle, res.cause());
								}
							});
						} else {
							LOGGER.info("++++++++++++++++++++ Not deploy verticle {} ++++++++++++++++++++", nameOfVerticle);
						}
					});
		}
	}
}