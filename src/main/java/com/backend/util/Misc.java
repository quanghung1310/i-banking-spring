package com.backend.util;

import com.backend.config.MainConfig;
import com.backend.constant.StringConstant;
import com.backend.cryption.RSACryption;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

public class Misc {
    private static final Logger LOGGER = LogManager.getLogger(Misc.class);

    /**
     * Convert phone number from 10 to 11 number
     *
     * @param phoneNumber phone need convert
     * @return phone converted to 11 number
     */
    public static String convertToOldPhone(String phoneNumber) {
        JsonObject listPhone = MainConfig.listPhoneConvert();
        if (listPhone == null || listPhone.isEmpty()) {
            return phoneNumber;
        }
        // phoneNumber=0363198462
        for (String key : listPhone.fieldNames()) {
            // key=036; value=0166
            if (phoneNumber.startsWith(key)) {
                String value = listPhone.getString(key);
                // return 01663198462
                return value + phoneNumber.substring(key.length());
            }
        }
        return phoneNumber;
    }

    public static String parseRSAData(String logId, String hash, String key) {
        String partnerRawData = "";
        try {
            partnerRawData = new RSACryption().decryptRSA(hash.trim(), key.trim());
            LOGGER.info("{}| Parse RSA data get result: {}", logId, partnerRawData.replaceAll("[\\s\n\t]", ""));
            return partnerRawData;
        } catch (Exception e) {
            LOGGER.error("{}| Parse RSA data get exception: {}", logId, e.getMessage());
        }
        return partnerRawData;
    }

    public static String encryptRSA(String logId, String data, String pub_key) {
        String hash = "";
        try {
            byte[] testByte = data.getBytes(StandardCharsets.UTF_8);
            hash = new RSACryption().encryptRSA(testByte, pub_key).replaceAll("\n", "");
            LOGGER.info("{}| Create RSA data get result: {}", logId, hash);
        } catch (Exception e) {
            LOGGER.error("{}| Create RSA data get exception: {}", logId, e.getMessage());
        }
        return hash;
    }

    public static String genRSAKey(String logId) {
        String result = "";
        try {
            JsonObject key = new RSACryption().genPriPubKeyByRSA();
            result = key.toString();
            LOGGER.info("{}| Generate private key: {}", logId, key.getString(StringConstant.PRIVATE_KEY, ""));
            LOGGER.info("{}| Generate public key: {}", logId, key.getString(StringConstant.PUBLIC_KEY, ""));
        } catch (Exception e) {
            LOGGER.error("{}| Generate RSA key get exception: {}", logId, e.getMessage());
        }
        return result;
    }
}
