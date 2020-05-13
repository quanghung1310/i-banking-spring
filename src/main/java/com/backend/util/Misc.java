package com.backend.util;

import com.backend.config.MainConfig;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Misc {
    private static final Logger LOGGER = LogManager.getLogger(Misc.class);

    /**
     * Convert phone number from 10 to 11 number
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
}
