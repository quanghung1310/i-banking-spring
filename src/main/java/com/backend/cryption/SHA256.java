package com.backend.cryption;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Set;
import java.util.TreeSet;

public class SHA256 {
    private static final Logger LOGGER = LogManager.getLogger(SHA256.class);
    public static final String HMAC_SHA256 = "HmacSHA256";

    public static String createHash(JsonObject json, String secretKey, String logId) {
        try {
            JsonObject jsonSimple = toSimpleJsonObject(new JsonObject(), json, true);
            String dataCryption = buildRawDataSignature(jsonSimple);
            LOGGER.info("{}| Data cryption: {}", logId, dataCryption);
            return signHmacSHA256(dataCryption, secretKey);
        } catch (Exception ex) {
            return "";
        }
    }

    public static JsonObject toSimpleJsonObject(JsonObject jsonSimple, JsonObject jsonComplex, boolean isToString) {
        Set<String> setKeys = jsonComplex.fieldNames();
        for (String key : setKeys) {
            Object value = jsonComplex.getValue(key);
            if (value != null && !(value instanceof String) && value.toString().startsWith("{") && value.toString().endsWith("}")) {
                toSimpleJsonObject(jsonSimple, new JsonObject(value.toString()), isToString);
            } else {
                jsonSimple.put(key, isToString ? value != null ? value.toString() : "" : value);
            }
        }
        return jsonSimple;
    }

    public static String buildRawDataSignature(JsonObject rawData) {
        StringBuilder result = new StringBuilder();
        if (rawData == null) {
            return result.toString();
        }
        Set<String> setKeys = rawData.fieldNames();
        Set<String> keysSorted = new TreeSet<>(setKeys);
        for (String key : keysSorted) {
            result.append("&").append(key).append("=").append(rawData.getValue(key, ""));
        }
        return result.substring(1);
    }

    public static String signHmacSHA256(String data, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        return signHmacSHA(data, secretKey, HMAC_SHA256);
    }

    public static String signHmacSHA(String data, String secretKey, String type) throws NoSuchAlgorithmException, InvalidKeyException {
        return toHexString(signHmacSHARaw(data, secretKey, type));
    }

    private static String toHexString(byte[] bytes) {
        @SuppressWarnings("resource")
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static byte[] signHmacSHARaw(String data, String secretKey, String type) throws InvalidKeyException, NoSuchAlgorithmException {
        return signHmacSHARaw(data.getBytes(StandardCharsets.UTF_8), secretKey, type);
    }

    public static byte[] signHmacSHARaw(byte[] data, String secretKey, String type) throws NoSuchAlgorithmException, InvalidKeyException {
        return signHmacSHARaw(data, secretKey.getBytes(), type);
    }

    public static byte[] signHmacSHARaw(byte[] data, byte[] secretKey, String type) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, type);
        Mac mac = Mac.getInstance(type);
        mac.init(secretKeySpec);
        return mac.doFinal(data);
    }
}
