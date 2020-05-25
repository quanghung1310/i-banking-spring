package com.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class DataUtil {

    private static final Logger LOGGER = LogManager.getLogger(DataUtil.class);
    public static final String HMAC_SHA256 = "HmacSHA256";

    private static DataUtil instance;

    private static final ObjectMapper mapper = new ObjectMapper();

    private DataUtil() {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("  ", "\n");
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter() {
            @Override
            public DefaultPrettyPrinter withSeparators(Separators separators) {
                _separators = separators;
                _objectFieldValueSeparatorWithSpaces = separators.getObjectFieldValueSeparator() + " ";
                return this;
            }
        };
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
        mapper.setDefaultPrettyPrinter(printer);
    }

    public static void init() {
        instance = new DataUtil();
    }

    public static DataUtil getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    public String toString2SpaceWithSort(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public static String convertTimeWithFormat(long timeInMillisecond, String format) {
        try {
            DateFormat f = new SimpleDateFormat(format);
            return f.format(timeInMillisecond);
        } catch (Exception e) {
            return "";
        }
    }

    public static String formatAmount(long amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount).replace(",", ".");
    }

    public static String createRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String createMD5(String data) {
        // create the md5 hash and UTF-8 encode it
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] ba = md5.digest(data.getBytes(StandardCharsets.UTF_8));
            return hex(ba);
        } catch (Exception ignored) {}
        return "";
    }

    private static String hex(byte[] input) {
        // create a StringBuilder 2x the size of the hash array
        StringBuilder sb = new StringBuilder(input.length * 2);

        // retrieve the byte array data, convert it to hex
        // and add it to the StringBuilder
        for (byte b : input) {
            String h = Integer.toHexString(0xFF & b);
            while (h.length() < 2) {
                h = "0" + h;
            }
            sb.append(h);
        }
        return sb.toString();
    }

    public static JsonObject sortJson(JsonObject rawJson) {
        Set<String> setKeys = rawJson.fieldNames();
        Set<String> keysSorted = new TreeSet<>(setKeys);

        JsonObject sortedJson = new JsonObject();
        for (String key : keysSorted) {
            Object value = rawJson.getValue(key);
            sortedJson.put(key, checkObjectSort(value));
        }
        return sortedJson;
    }

    private static Object checkObjectSort(Object value) {
        if (value != null) {
            String valueStr = value.toString();
            if (valueStr.startsWith("{") && valueStr.endsWith("}")) {
                return sortJson(new JsonObject(valueStr));
            } else if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
                JsonArray arrRaw = new JsonArray(valueStr);
                JsonArray arrSort = new JsonArray();
                for (Object obj : arrRaw) {
                    arrSort.add(checkObjectSort(obj));
                }
                return arrSort;
            } else {
                return value;
            }
        } else {
            return null;
        }
    }

    public static String createHash(JsonObject json, String secretKey, String logId) {
        try {
            JsonObject jsonSimple = toSimpleJsonObject(new JsonObject(), json, true);
            String dataCryption = buildRawDataHash(jsonSimple);
            LOGGER.info("{}| MoMo data cryption: {}", logId, dataCryption);
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

    public static String buildRawDataHash(JsonObject rawData) {
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

    public static JsonObject maskedJson(JsonObject rawJson) {
        JsonObject result = rawJson.copy();
        if (result.containsKey("pass")) {
            result.put("pass", "******");
        }
        if (result.containsKey("pin")) {
            result.put("pin", "******");
        }
        if (result.containsKey("customerPin")) {
            result.put("customerPin", "******");
        }
        if (result.containsKey("debitorPin")) {
            result.put("debitorPin", "******");
        }
        if (result.containsKey("new_pin")) {
            result.put("new_pin", "******");
        }
        JsonObject resultReq = result.getJsonObject("momoMsg", new JsonObject()).getJsonObject("paymentInfo");
        if (resultReq != null) {
            if (resultReq.containsKey("pass")) {
                resultReq.put("pass", "******");
            }
            if (resultReq.containsKey("pin")) {
                resultReq.put("pin", "******");
            }
            if (resultReq.containsKey("debitorPin")) {
                resultReq.put("debitorPin", "******");
            }
            if (resultReq.containsKey("customerPin")) {
                resultReq.put("customerPin", "******");
            }
        }
        return result;
    }
}
