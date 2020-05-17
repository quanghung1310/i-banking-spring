package com.backend.database;

import com.backend.constant.SqlConstant;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MySqlManager extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(MySqlManager.class.getName());

    public static void getPartnerByPartnerCode(String logId, String partnerCode, Handler<JsonObject> callback) {
        DatabaseVerticle.jdbcClient.queryWithParams(SqlConstant.QUERY_GET_PARTNER_BY_PARTNER_CODE, new JsonArray().add(partnerCode), res -> {
            if (res.succeeded()) {
                JsonObject result = res.result().getRows().isEmpty()? new JsonObject() : res.result().getRows().get(0);
                LOGGER.info("{}| Get Partner By PartnerCode result: {}", logId, result.copy().remove("PARTNER_SECRET_KEY"));
                callback.handle(result);
            } else {
                LOGGER.error("{}| Get Partner By PartnerCode catch exception:", logId, res.cause());
                callback.handle(new JsonObject());
            }
        });
    }
}
