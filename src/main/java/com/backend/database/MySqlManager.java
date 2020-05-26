package com.backend.database;

import com.backend.constant.SqlConstant;
import com.backend.dto.AccountDTO;
import com.backend.dto.PartnerDTO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MySqlManager {
    private MySqlManager() {}
    private static final Logger logger = LogManager.getLogger(MySqlManager.class.getName());

    public static void getPartnerByPartnerCode(String logId, String partnerCode, Handler<PartnerDTO> callback) {
        DatabaseVerticle.jdbcClient.queryWithParams(
                SqlConstant.QUERY_GET_PARTNER_BY_PARTNER_CODE,
                new JsonArray().add(partnerCode),
                res -> {
            if (res.succeeded()) {
                JsonObject result = res.result().getRows().isEmpty()? new JsonObject() : res.result().getRows().get(0);
                logger.info("{}| Get Partner By PartnerCode result: {}", logId, result.copy().remove("PARTNER_SECRET_KEY"));
                //todo: build to partner object
                callback.handle(new PartnerDTO());
            } else {
                logger.error("{}| Get Partner By PartnerCode catch exception:", logId, res.cause());
                callback.handle(null);
            }
        });
    }

    public static void getAccountBankInfo(String logId, String numberAccount, int typeAccount, Handler<AccountDTO> callback) {
        //todo: get account info
    }

    public static void insertPartner(String logId, String partnerCode, String privateKey, String publicKey, String email, String phoneNumber, String password, Handler<JsonObject> callback) {
        DatabaseVerticle.jdbcClient.queryWithParams(
                SqlConstant.QUERY_INSERT_PARTNER,
                new JsonArray().add(partnerCode)
                                .add(privateKey)
                                .add(publicKey)
                                .add(email)
                                .add(phoneNumber)
                                .add(password),
                res -> {
                    if (res.succeeded()) {
                        logger.info("{}| Insert partner success with partner code: {}", logId, partnerCode);
                    }
                    else {
                        logger.warn("{}| Insert partner fail!", logId);
                    }
                });
    }

}
