package com.backend.util;


import com.backend.constants.ErrorConstant;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.UserResponse;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class DataUtil {
    private static final Logger logger = LogManager.getLogger(DataUtil.class);

    public static final String HMAC_SHA256 = "HmacSHA256";

    public static String createRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static BaseResponse buildResponse(int resultCode, String requestId, String responseBody) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setResultCode(resultCode);
        baseResponse.setMessage(ErrorConstant.getMessage(resultCode));
        baseResponse.setResponseTime(System.currentTimeMillis());
        baseResponse.setRequestId(requestId);
        if (responseBody != null) {
            baseResponse.setData(new JsonObject(responseBody));
        }

        return baseResponse;
    }

    public static String convertTimeWithFormat(long timeInMillisecond, String format) {
        try {
            DateFormat f = new SimpleDateFormat(format);
            return f.format(timeInMillisecond);
        } catch (Exception e) {
            return "";
        }
    }

    public static String convertPublicKeyToString(PublicKey publicKey) {
        String plk = "";
        BASE64Encoder base64Encoder = new BASE64Encoder();
        plk = base64Encoder.encode(publicKey.getEncoded()).replaceAll("\\r\\n|\\r|\\n|", "");
        return plk;
    }

    public static String convertPrivateKeyToString(PrivateKey privateKey) {
        String prkey = "";
        BASE64Encoder base64Encoder = new BASE64Encoder();
        prkey = base64Encoder.encode(privateKey.getEncoded()).replaceAll("\\r\\n|\\r|\\n", "");
        return prkey;
    }

    public static String pgpPubKeyToString(PGPPublicKey publicKey) throws IOException {
        String secKey = "";
        BASE64Encoder base64Encoder = new BASE64Encoder();
        secKey = base64Encoder.encode(publicKey.getEncoded()).replaceAll("\\r\\n|\\r|\\n", "");
        return secKey;
    }

    public static String pgpSecretKeyToString(PGPSecretKey secretKey) throws IOException {
        String secKey = "";
        BASE64Encoder base64Encoder = new BASE64Encoder();
        secKey = base64Encoder.encode(secretKey.getEncoded()).replaceAll("\\r\\n|\\r|\\n", "");
        return secKey;
    }
    public static String createHash(JsonObject json, String secretKey, String logId) {
        try {
            JsonObject jsonSimple = toSimpleJsonObject(new JsonObject(), json, true);
            String dataCryption = buildRawDataHash(jsonSimple);
            logger.info("{}| Data crypto: {}", logId, dataCryption);
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

    public static ResponseEntity<String> getStringResponseEntity(String logId, UserResponse userResponse) {
        BaseResponse response;
        if (userResponse == null) {
            logger.warn("{}| query account fail!", logId);
            response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
            return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, userResponse.toString());
        response.setData(new JsonObject(userResponse.toString()));
        logger.info("{}| Response to client: {}", logId, userResponse.toString());
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }


}
