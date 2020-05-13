package com.backend.util;

import com.backend.config.MainConfig;
import com.backend.config.PartnerConfig;
import com.backend.model.Cryption;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            partnerRawData = new Cryption().decryptRSA(hash.trim(), key.trim());
            LOGGER.info("{}| Parse RSA data get result: {}", logId, partnerRawData.replaceAll("[\\s\n\t]", ""));
            return partnerRawData;
        } catch (Exception e) {
            LOGGER.error("{}| Parse RSA data get exception: {}", logId, e.getMessage());
        }
        return partnerRawData;
    }

    public static String encryptRSA(String logId, JsonObject data, String pub_key) {
        String hash = "";
        try {
//            String pub_key = PartnerConfig.getPublicKey();
//            JsonObject jsonDataClient = new JsonObject()
//                    .put("partnerCode", "lang1");
            byte[] testByte = data.toString().getBytes("UTF-8");
            hash = new Cryption().encryptRSA(testByte, pub_key).replaceAll("\n", "");
            LOGGER.info("{}| Create RSA data get result: {}", logId, hash);
        } catch (Exception e) {
            LOGGER.error("{}| Create RSA data get exception: {}", logId, e.getMessage());
        }
        return hash;
    }
}
