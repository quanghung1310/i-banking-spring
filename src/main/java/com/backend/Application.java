package com.backend;

import com.backend.config.PartnerConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.vertx.core.json.JsonObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.xml.ws.Response;

@SpringBootApplication
public class Application {
	private static final String PATH_TO_CONFIG_FOLDER = "conf\\";
	private static final ObjectWriter OBJECT_WRITER = new ObjectMapper().writer()
			.withDefaultPrettyPrinter();
	;

	public static void main(String[] args) {
		PartnerConfig.init(PATH_TO_CONFIG_FOLDER + "partner.json");
		SpringApplication.run(Application.class, args);
	}
//
//	@Bean
//	public RestTemplate restTemplate(RestTemplateBuilder builder) {
//		return builder.build();
//	}

//	public static void main(String[] args) {
//		String url = "https://yasuobank.herokuapp.com/api/v1/associate-bank/Lh/account-info";
//		RestTemplate restTemplate = new RestTemplate();
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
//		JsonObject requestJson = new JsonObject()
//				.put("payload", "-----BEGIN PGP MESSAGE-----\r\nVersion: OpenPGP.js v4.10.7\r\nComment: https://openpgpjs.org\r\n\r\nwcFMAyMzuTcpSL8EARAAqmRSsCcQSFt7+YG+e0t+VgYkV4ypINE2eysr8aeF\r\ng1wJl2TWu3JEavsEUJ5pwSTwv8dJ1pRGVmgphkBMAo2xJdkiqg4El+EruHx3\r\n33hz9RdioSO6aoo4BmZEQH+TGBhDxGlAXnQj1qGeUcAEVi4g4hmXHcxnqdNt\r\n1faVKy/jLm2ovzpq/B/b113RwHRKEcVM4unTWEhxN4bTf4WuqAJxc+vSVx4X\r\nicrX9MLtYmwxAr/Xl287vwjLnb3vrL+gHnNmFEapzAvC5IPgPbHz9rYj3xr6\r\nl4O49+zGqiHJT3NaDSwqzt6HUbv1YBJ/gMstvAj0r5huLz5E62l/hIVncgX8\r\n+Z/la6zJ+F6P5W7EW/Lox7RT6h8z/AFxaJcOU1TROhi2npDBbKPvITffBqr/\r\nq+L6ci05e+herDN/cQ3J2mIcPZh/uAfPcZCYNPAuAea2dI4wFlQx7cRP6oSz\r\nsd9oKWQr0OTENZsKLdlrIiwSr55oTiUVGO1fiBiamYqKQG29rPA5hM3FICQY\r\nFV0gx4StpNOk2fLYtFsZ0v3XOEcMMOknu9dCpoW10hNVPQzACuqzcoZcHZrk\r\nwZMLHC1x6sfcNkcbtwhWiEqtQr63Y+7gznnN0rg6akHlf/1fCeVPGbtGZLUm\r\nsox9xyH+YNJgw5WCSzywqZiVvk8BjTjv7QiT9iTN7k3SUwFeyZRGv2JiqNA9\r\nkQlUSrnyqfckTtwK1xtpnDNfg61P11c0KJHQfZBnlJLSZ2Y9qcM2cgfTeU/2\r\nSH1tFHLuIt0TBsmMrZcZzFZqERq1pnSQgVN1\r\n=lzji\r\n-----END PGP MESSAGE-----\r\n")
//				.put("signature", "3bd006eed1477ced1223267c70b81b7921e00e8f7e3e00d2bb482a78ac59c704e9d8bba52d02ada2b3c68221eafc9126dd045bf75b36aaeeac60ead03ce72c76");
//
//
//		HttpEntity<String> entity = new HttpEntity<>(requestJson.toString(), headers);
//		String answer = restTemplate.postForObject(url, entity, String.class);
//		String result = restTemplate.postForObject(
//				url,
//				requestJson,
//				String.class);
//
//	}
}