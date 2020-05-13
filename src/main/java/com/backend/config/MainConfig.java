package com.backend.config;

import io.vertx.core.json.JsonObject;
import jdk.nashorn.internal.runtime.options.Options;

public class MainConfig {
    private static JsonObject mainConfig;
    private static String emailOtpTemplate;

    private MainConfig() {};

    public static void setMainConfig(JsonObject mainConfig) {
        MainConfig.mainConfig = mainConfig;
    }

    public static JsonObject listPhoneConvert() {
        return new JsonObject();
    }
}
