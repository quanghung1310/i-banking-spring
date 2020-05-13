package com.backend.config;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PartnerConfig {
    private static JsonObject partnerConfig;

    public static void init(String partnerSystemConfigPath) {
        try {
            partnerConfig = new JsonObject(new String(Files.readAllBytes(Paths.get(partnerSystemConfigPath))));
        } catch (IOException e) {
            e.printStackTrace();
            partnerConfig = new JsonObject();
        }
    }

    private static JsonObject getMerchantConfig() {
        return partnerConfig.getJsonObject("lang1", new JsonObject());
    }
    public static String getPrivateKey() {
        return getMerchantConfig().getString("private_key", "");
    }
    public static String getPublicKey() {
        return getMerchantConfig().getString("public_key", "");
    }
    public static String getSecretKey() {
        return getMerchantConfig().getString("secret_key", "");
    }
}
