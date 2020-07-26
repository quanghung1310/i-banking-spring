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

    public static JsonObject getMerchantConfig(String id) {
        return partnerConfig.getJsonObject(id, new JsonObject());
    }
    public static String getPrivateKey(String id) {
        return getMerchantConfig(id).getString("private_key", "");
    }
    public static String getPublicKey(String id) {
        return getMerchantConfig(id).getString("public_key", "");
    }
    public static String getSecretKey(String id) {
        return getMerchantConfig(id).getString("secret_key", "");
    }
    public static String getAlg(String id) {
        return getMerchantConfig(id).getString("alg", "");
    }
    public static String getUrlQueryAccount(String id) {
        return getMerchantConfig(id).getString("url_query_account", "");
    }
    public static String getUrlTransfer(String id) {
        return getMerchantConfig(id).getString("url_transfer", "");
    }
    public static String getPartnerCode(String id) {
        return getMerchantConfig(id).getString("partner_code", "");
    }
    public static String getPartnerPubKey(String id) {
        return getMerchantConfig(id).getString("partner_public_key", "");
    }

}
